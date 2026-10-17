package com.ts.ds.compose.components.button

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.assertTouchWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSButtonSize
import com.ts.ds.core.style.TSButtonVariant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSButtonTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var taps = 0

    private fun setButton(
        variant: TSButtonVariant = TSButtonVariant.Primary,
        size: TSButtonSize = TSButtonSize.Md,
        enabled: Boolean = true,
        loading: Boolean = false,
        leadingIcon: TSIcon? = null,
    ) {
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                Box(Modifier.testTag("container")) {
                    TSButton(
                        text = "Save",
                        onClick = { taps++ },
                        variant = variant,
                        size = size,
                        enabled = enabled,
                        loading = loading,
                        leadingIcon = leadingIcon,
                        modifier = Modifier.testTag("button"),
                    )
                }
            }
        }
    }

    @Test
    fun clickInvokesTheAction() {
        setButton()

        composeRule.onNodeWithTag("button").assertHasClickAction().assertIsEnabled().performClick()

        assertEquals(1, taps)
    }

    @Test
    fun exposesButtonRoleAndTheLabelAsText() {
        setButton(leadingIcon = TSIcon.Check)

        composeRule.onNodeWithTag("button")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Text, listOf(AnnotatedString("Save"))))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
    }

    @Test
    fun disabledBlocksTheAction() {
        setButton(enabled = false)

        composeRule.onNodeWithTag("button").assertIsNotEnabled().performClick()

        assertEquals(0, taps)
    }

    @Test
    fun loadingBlocksTheActionAndReportsLoading() {
        setButton(loading = true, leadingIcon = TSIcon.Check)

        composeRule.onNodeWithTag("button")
            .assertIsNotEnabled()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Loading"))
            .performClick()
        composeRule.onNodeWithText("Save").assertExists()

        assertEquals(0, taps)
    }

    @Test
    fun loadingWinsOverDisabledButStaysBlocked() {
        setButton(enabled = false, loading = true)

        composeRule.onNodeWithTag("button")
            .assertIsNotEnabled()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Loading"))
    }

    @Test
    fun mediumButtonIs44dpTallInsideA48dpTarget() {
        setButton()

        composeRule.onNodeWithTag("container").assertHeightIsEqualTo(48.dp)
        composeRule.onNodeWithTag("button").assertTouchHeightIsEqualTo(48.dp)
    }

    @Test
    fun largeButtonIs52dpTall() {
        setButton(size = TSButtonSize.Lg)

        composeRule.onNodeWithTag("container").assertHeightIsEqualTo(52.dp)
    }

    @Test
    fun linkHasAtLeastA48dpTouchTarget() {
        setButton(variant = TSButtonVariant.Link)

        composeRule.onNodeWithTag("button")
            .assertTouchHeightIsEqualTo(48.dp)
            .assertTouchWidthIsEqualTo(48.dp)
            .performClick()

        assertEquals(1, taps)
    }

    @Test
    fun iconsAndSpinnerAreHiddenFromAccessibility() {
        setButton(leadingIcon = TSIcon.Plus)
        composeRule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image), useUnmergedTree = true).assertCountEquals(0)
    }
}
