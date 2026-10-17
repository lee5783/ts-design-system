package com.ts.ds.compose.components.checkbox

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.assertTouchWidthIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.haptics.DsHapticEvent
import com.ts.ds.core.haptics.DsHaptics
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSCheckboxTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val changes = mutableListOf<Boolean>()
    private lateinit var hostView: View

    private fun setCheckbox(initial: Boolean = false, enabled: Boolean = true, labelHidden: Boolean = false, hapticsEnabled: Boolean = true) {
        composeRule.setContent {
            hostView = LocalView.current
            TSTheme(darkTheme = false, hapticsEnabled = hapticsEnabled) {
                var checked by remember { mutableStateOf(initial) }
                Box(Modifier.testTag("container")) {
                    TSCheckbox(
                        checked = checked,
                        label = "Remember me",
                        onCheckedChange = {
                            changes += it
                            checked = it
                        },
                        labelHidden = labelHidden,
                        enabled = enabled,
                        modifier = Modifier.testTag("checkbox"),
                    )
                }
            }
        }
    }

    @Test
    fun tapOnTheLabelTogglesAndReportsTheNewValue() {
        setCheckbox()

        composeRule.onNodeWithText("Remember me").assertIsOff().performClick()
        composeRule.onNodeWithText("Remember me").assertIsOn()
        composeRule.onNodeWithTag("checkbox").performClick()

        assertEquals(listOf(true, false), changes)
    }

    @Test
    fun exposesTheCheckboxRoleToggleStateAndStateDescription() {
        setCheckbox(initial = true)

        composeRule.onNodeWithTag("checkbox")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.On))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Checked"))
            .performClick()
        composeRule.onNodeWithTag("checkbox")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Unchecked"))
    }

    @Test
    fun disabledBlocksToggling() {
        setCheckbox(enabled = false)

        composeRule.onNodeWithTag("checkbox").assertIsNotEnabled().performClick()

        assertEquals(emptyList<Boolean>(), changes)
    }

    @Test
    fun wholeRowIsTheHitAreaAndAtLeast48dpTall() {
        setCheckbox()

        composeRule.onNodeWithTag("checkbox").assertHeightIsEqualTo(48.dp).performTouchInput { click(centerRight.copy(x = right - 1f)) }

        assertEquals(listOf(true), changes)
    }

    @Test
    fun hiddenLabelKeepsTheLabelForAccessibilityAndA48dpTarget() {
        setCheckbox(labelHidden = true)

        composeRule.onAllNodesWithText("Remember me").assertCountEquals(0)
        composeRule.onNodeWithContentDescription("Remember me").assertIsOff()
        composeRule.onNodeWithTag("container").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(48.dp)
        composeRule.onNodeWithTag("checkbox").assertTouchWidthIsEqualTo(48.dp).assertTouchHeightIsEqualTo(48.dp)
    }

    @Test
    fun togglingPlaysTheToggleHaptic() {
        setCheckbox()

        composeRule.onNodeWithTag("checkbox").performClick()
        assertEquals(DsHaptics.constantFor(DsHapticEvent.ToggleOn), shadowOf(hostView).lastHapticFeedbackPerformed())
        composeRule.onNodeWithTag("checkbox").performClick()
        assertEquals(DsHaptics.constantFor(DsHapticEvent.ToggleOff), shadowOf(hostView).lastHapticFeedbackPerformed())
    }

    @Test
    fun hapticsOptOutIsHonoured() {
        setCheckbox(hapticsEnabled = false)

        composeRule.onNodeWithTag("checkbox").performClick()

        assertEquals(-1, shadowOf(hostView).lastHapticFeedbackPerformed())
        assertEquals(listOf(true), changes)
    }
}
