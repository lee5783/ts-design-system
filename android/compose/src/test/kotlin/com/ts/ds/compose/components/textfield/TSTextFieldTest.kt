package com.ts.ds.compose.components.textfield

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.assertTouchWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.input.TSImeAction
import com.ts.ds.core.input.TSTextInputTraits
import com.ts.ds.core.style.TSTextFieldContentStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSTextFieldTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var text by mutableStateOf("")
    private var submits = 0

    private fun setField(
        initial: String = "",
        placeholder: String? = null,
        hint: String? = HINT,
        error: String? = null,
        leadingIcon: TSIcon? = null,
        secure: Boolean = false,
        multiline: Boolean = false,
        readOnly: Boolean = false,
        enabled: Boolean = true,
        inputTraits: TSTextInputTraits = TSTextInputTraits.Default,
    ) {
        text = initial
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                Box(Modifier.width(320.dp)) {
                    TSTextField(
                        value = text,
                        label = LABEL,
                        onValueChange = { text = it },
                        modifier = Modifier.testTag(FIELD),
                        placeholder = placeholder,
                        hint = hint,
                        error = error,
                        leadingIcon = leadingIcon,
                        secure = secure,
                        multiline = multiline,
                        readOnly = readOnly,
                        enabled = enabled,
                        inputTraits = inputTraits,
                        onSubmit = { submits++ },
                    )
                }
            }
        }
    }

    private fun field() = composeRule.onNodeWithTag(FIELD)

    private fun texts(): List<String> =
        field().fetchSemanticsNode().config.getOrElse(SemanticsProperties.Text) { emptyList() }.map(AnnotatedString::text)

    @Test
    fun typingUpdatesTheValue() {
        setField()

        field().performTextInput("ada@example.com")

        assertEquals("ada@example.com", text)
        field().assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("ada@example.com")))
    }

    @Test
    fun labelAndHintAreShown() {
        setField()

        assertEquals(listOf(LABEL, HINT), texts())
    }

    @Test
    fun errorReplacesTheHint() {
        setField(error = ERROR)

        assertEquals(listOf(LABEL, ERROR), texts())
    }

    @Test
    fun disabledBlocksEditing() {
        setField(initial = "Ada", enabled = false)

        field().assertIsNotEnabled().assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetText))
        assertEquals("Ada", text)
    }

    @Test
    fun readOnlyIsEnabledAndSelectableButNotEditable() {
        setField(initial = "TS-2048", readOnly = true)

        field()
            .assertIsEnabled()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.SetSelection))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetText))
    }

    @Test
    fun readOnlySecureTextStaysMaskedWithoutAToggle() {
        setField(initial = "secret", secure = true, readOnly = true)

        field().assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
        composeRule.onAllNodesWithTag(TSTextFieldTestTags.PASSWORD_TOGGLE).assertCountEquals(0)
    }

    @Test
    fun secureToggleRevealsAndHidesThePassword() {
        setField(initial = "secret", secure = true)

        field().assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
        composeRule.onNodeWithTag(TSTextFieldTestTags.PASSWORD_TOGGLE).performClick()

        field().assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Password))
        composeRule.onNodeWithTag(TSTextFieldTestTags.PASSWORD_TOGGLE).performClick()

        field().assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
        assertEquals("secret", text)
    }

    @Test
    fun passwordToggleHasA48dpTouchTarget() {
        setField(secure = true)

        composeRule.onNodeWithTag(TSTextFieldTestTags.PASSWORD_TOGGLE)
            .assertTouchHeightIsEqualTo(48.dp)
            .assertTouchWidthIsEqualTo(48.dp)
    }

    @Test
    fun singleLineFieldIs44dpTallUnderTheLabel() {
        setField(hint = null)

        field().assertHeightIsEqualTo(68.dp)
    }

    @Test
    fun multilineStartsAtThreeLinesAndStopsGrowingAtSix() {
        setField(hint = null, multiline = true)
        field().assertHeightIsEqualTo(116.dp)

        text = (1..4).joinToString("\n") { "Line $it" }
        field().assertHeightIsEqualTo(140.dp)

        text = (1..10).joinToString("\n") { "Line $it" }
        field().assertHeightIsEqualTo(188.dp)
    }

    @Test
    fun clickingFocusesTheField() {
        setField()

        field().performClick().assertIsFocused()
    }

    @Test
    fun imeActionSubmits() {
        setField(inputTraits = TSTextInputTraits.Default.copy(imeAction = TSImeAction.Done))

        field().performImeAction()

        assertEquals(1, submits)
    }

    @Test
    fun inputTraitsSetTheAutofillContentType() {
        setField(inputTraits = TSTextInputTraits.Email)

        field().assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentType))
    }

    @Test
    fun defaultTraitsSetNoAutofillContentType() {
        setField()

        field().assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentType))
    }

    @Test
    fun codeTraitsOpenAPasswordKeyboardWithoutSuggestionsInCapitals() {
        val expected = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Default,
        )

        assertEquals(expected, TSTextInputTraits.Code.keyboardOptions(masked = false))
        assertEquals(KeyboardType.Password, TSTextInputTraits.Code.keyboardOptions(masked = true).keyboardType)
        assertEquals(KeyboardType.Ascii, TSTextInputTraits.Code.copy(autoCorrect = true).keyboardOptions(masked = false).keyboardType)
    }

    @Test
    fun codeFieldShowsItsTextUnmaskedInTheMonospaceCodeRole() {
        text = "AB12CD"
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                Box(Modifier.width(320.dp)) {
                    TSTextField(
                        value = text,
                        label = CODE_LABEL,
                        onValueChange = { text = it },
                        contentStyle = TSTextFieldContentStyle.Code,
                        modifier = Modifier.testTag(FIELD),
                        hint = HINT,
                        inputTraits = TSTextInputTraits.Code,
                    )
                }
            }
        }

        field().assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Password))
        field().assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("AB12CD")))
        val inputLayout = field().textLayout().layoutInput
        assertEquals("AB12CD", inputLayout.text.text)
        val inputStyle = inputLayout.style
        assertEquals(FontFamily.Monospace, inputStyle.fontFamily)
        assertEquals(13.sp, inputStyle.fontSize)
        val labelStyle = composeRule.onNodeWithText(CODE_LABEL, useUnmergedTree = true).textLayout().layoutInput.style
        assertNotEquals(FontFamily.Monospace, labelStyle.fontFamily)
        field().assertHeightIsEqualTo(88.dp)
    }

    @Test
    fun latinLabelAndHintKeepALeftToRightParagraphInAnRtlLayout() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                TSTheme(darkTheme = false) {
                    Box(Modifier.width(320.dp)) {
                        TSTextField(value = text, label = LABEL, onValueChange = { text = it }, hint = HINT)
                    }
                }
            }
        }
        listOf(LABEL, HINT).forEach { value ->
            val layout = composeRule.onNodeWithText(value, useUnmergedTree = true).textLayout()
            assertEquals(value, ResolvedTextDirection.Ltr, layout.getParagraphDirection(0))
            assertEquals(value, TextAlign.Right, layout.layoutInput.style.textAlign)
        }
    }

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.single()
    }

    private companion object {
        const val FIELD = "field"
        const val LABEL = "Email"
        const val CODE_LABEL = "Join code"
        const val HINT = "We never share your email."
        const val PLACEHOLDER = "you@example.com"
        const val ERROR = "Enter an email like name@example.com"
    }
}
