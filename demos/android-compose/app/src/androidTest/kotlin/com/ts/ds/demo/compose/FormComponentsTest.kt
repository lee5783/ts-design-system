package com.ts.ds.demo.compose

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.AnnotatedString
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.compose.components.CheckboxPageSpec
import com.ts.ds.demo.compose.components.DemoTogglePageSpec
import com.ts.ds.demo.compose.components.SwitchPageSpec
import com.ts.ds.demo.compose.components.TextFieldPageTags
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormComponentsTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    @After
    fun resetSettings() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { settingsStore.reset() }
    }

    private fun ComposeTestRule.scrollTo(tag: String) {
        onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(tag))
    }

    private fun ComposeTestRule.tap(tag: String) {
        scrollTo(tag)
        onNodeWithTag(tag).performClick()
        waitForIdle()
    }

    private fun ComposeTestRule.field() = onNodeWithTag(TextFieldPageTags.PLAYGROUND_FIELD)

    private fun ComposeTestRule.toggle(label: String) =
        onNode(hasContentDescription(label) and hasAnyAncestor(hasTestTag(TextFieldPageTags.PLAYGROUND_FIELD)), useUnmergedTree = true)

    @Test
    fun textFieldTypingErrorAndPasswordToggle() {
        val error = resources.getString(DemoR.string.demo_sample_email_error)
        launchDemo().use {
            composeRule.openPage(DemoPageId.TextField)
            composeRule.scrollTo(TextFieldPageTags.PLAYGROUND_FIELD)
            composeRule.field().performClick().assertIsFocused().performTextInput("ada")
            composeRule.field()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("ada")))
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Error))

            composeRule.tap(TextFieldPageTags.PLAYGROUND_ERROR)
            composeRule.scrollTo(TextFieldPageTags.PLAYGROUND_FIELD)
            composeRule.field().assert(SemanticsMatcher.expectValue(SemanticsProperties.Error, error))

            composeRule.tap(TextFieldPageTags.PLAYGROUND_SECURE)
            composeRule.scrollTo(TextFieldPageTags.PLAYGROUND_FIELD)
            composeRule.field().assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
            composeRule.toggle("Show password").performClick()
            composeRule.field().assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Password))
            composeRule.toggle("Hide password").performClick()
            composeRule.field()
                .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("•••")))
        }
    }

    @Test
    fun codeFieldShowsTypedCodeUnmasked() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.TextField)
            composeRule.scrollTo(TextFieldPageTags.CODE_FIELD)
            composeRule.onNodeWithTag(TextFieldPageTags.CODE_FIELD).performClick().assertIsFocused().performTextInput("AB12CD")
            composeRule.onNodeWithTag(TextFieldPageTags.CODE_FIELD)
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("AB12CD")))
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Password))
        }
    }

    @Test
    fun disabledAndReadOnlyBlockEditing() {
        launchDemo().use {
            composeRule.openPage(DemoPageId.TextField)
            composeRule.tap(TextFieldPageTags.PLAYGROUND_DISABLED)
            composeRule.scrollTo(TextFieldPageTags.PLAYGROUND_FIELD)
            composeRule.field().assertIsNotEnabled().assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetText))

            composeRule.tap(TextFieldPageTags.PLAYGROUND_DISABLED)
            composeRule.tap(TextFieldPageTags.PLAYGROUND_READ_ONLY)
            composeRule.scrollTo(TextFieldPageTags.PLAYGROUND_FIELD)
            composeRule.field()
                .assertIsEnabled()
                .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.SetText))
                .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.SetSelection))
        }
    }

    private fun toggleTogglesFromTheLabelAndDisabledBlocks(page: DemoPageId, spec: DemoTogglePageSpec, onState: String, offState: String) {
        val on = resources.getString(spec.onRes)
        val off = resources.getString(spec.offRes)
        launchDemo().use {
            composeRule.openPage(page)
            composeRule.scrollTo(spec.playgroundCountTag)
            composeRule.onNodeWithTag(spec.playgroundControlTag)
                .assertIsOff()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, offState))
                .performTouchInput { click(centerRight.copy(x = right - 4f)) }
            composeRule.onNodeWithTag(spec.playgroundControlTag)
                .assertIsOn()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, onState))
            composeRule.onNodeWithTag(spec.playgroundValueTag).assertTextEquals(on)
            composeRule.onNodeWithTag(spec.playgroundControlTag).performClick()
            composeRule.onNodeWithTag(spec.playgroundValueTag).assertTextEquals(off)
            composeRule.onNodeWithTag(spec.playgroundCountTag).assertTextEquals("Tapped 2 times")

            composeRule.tap(spec.playgroundDisabledTag)
            composeRule.scrollTo(spec.playgroundCountTag)
            composeRule.onNodeWithTag(spec.playgroundControlTag).assertIsNotEnabled().performClick()
            composeRule.onNodeWithTag(spec.playgroundControlTag).assertIsOff()
            composeRule.onNodeWithTag(spec.playgroundCountTag).assertTextEquals("Tapped 2 times")
        }
    }

    @Test
    fun checkboxTogglesFromTheLabelAndDisabledBlocks() {
        toggleTogglesFromTheLabelAndDisabledBlocks(DemoPageId.Checkbox, CheckboxPageSpec, "Checked", "Unchecked")
    }

    @Test
    fun switchTogglesFromTheLabelAndDisabledBlocks() {
        toggleTogglesFromTheLabelAndDisabledBlocks(DemoPageId.Switch, SwitchPageSpec, "On", "Off")
    }
}
