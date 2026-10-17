package com.ts.ds.compose.foundation

import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.assertTouchWidthIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.haptics.DsHapticEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
class TSFoundationModifiersTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun smallControlsGrowToA48dpTarget() {
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                Box(Modifier.testTag("container")) {
                    Box(
                        Modifier
                            .tsMinimumTouchTarget()
                            .testTag("target")
                            .clickable { }
                            .size(20.dp),
                    )
                }
            }
        }

        composeRule.onNodeWithTag("container")
            .assertWidthIsEqualTo(48.dp)
            .assertHeightIsEqualTo(48.dp)
        composeRule.onNodeWithTag("target")
            .assertWidthIsEqualTo(20.dp)
            .assertTouchWidthIsEqualTo(48.dp)
            .assertTouchHeightIsEqualTo(48.dp)
    }

    @Test
    fun largerControlsKeepTheirSize() {
        composeRule.setContent {
            Box(Modifier.testTag("container")) {
                Box(Modifier.tsMinimumTouchTarget().size(width = 120.dp, height = 56.dp))
            }
        }

        composeRule.onNodeWithTag("container")
            .assertWidthIsEqualTo(120.dp)
            .assertHeightIsEqualTo(56.dp)
    }

    @Test
    fun focusRingShowsOnlyForKeyboardFocus() {
        assertTrue(isFocusRingVisible(focused = true, inputMode = InputMode.Keyboard))
        assertFalse(isFocusRingVisible(focused = true, inputMode = InputMode.Touch))
        assertFalse(isFocusRingVisible(focused = false, inputMode = InputMode.Keyboard))
        assertFalse(isFocusRingVisible(focused = false, inputMode = InputMode.Touch))
    }

    @Test
    fun focusRingTracksFocusInteractions() {
        val source = MutableInteractionSource()
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                Box(
                    Modifier
                        .testTag("field")
                        .size(44.dp)
                        .tsFocusRing(source, RoundedCornerShape(8.dp), Color.Magenta)
                        .focusable(interactionSource = source),
                )
            }
        }

        composeRule.onNodeWithTag("field").requestFocus()
        composeRule.onNodeWithTag("field").assertIsFocused()
    }

    @Test
    fun pressIndicationEqualityFollowsColor() {
        assertEquals(TSPressIndication(Color.Red), TSPressIndication(Color.Red))
        assertEquals(TSPressIndication(Color.Red).hashCode(), TSPressIndication(Color.Red).hashCode())
        assertNotEquals(TSPressIndication(Color.Red), TSPressIndication(Color.Blue))
    }

    @Test
    fun pressIndicationHandlesClicks() {
        var clicks = 0
        composeRule.setContent {
            TSTheme(darkTheme = false) {
                Box(Modifier.testTag("row").size(48.dp).clickable { clicks++ })
            }
        }

        composeRule.onNodeWithTag("row").performClick()
        composeRule.runOnIdle { assertEquals(1, clicks) }
    }

    @Test
    fun hapticsRespectTheThemeOptOut() {
        lateinit var enabled: TSHaptics
        lateinit var disabled: TSHaptics
        lateinit var view: View
        composeRule.setContent {
            view = LocalView.current
            TSTheme(darkTheme = false) { enabled = rememberTSHaptics() }
            TSTheme(darkTheme = false, hapticsEnabled = false) { disabled = rememberTSHaptics() }
        }

        composeRule.runOnIdle {
            view.isHapticFeedbackEnabled = true
            assertTrue(enabled.enabled)
            assertFalse(disabled.enabled)
            assertFalse(disabled.perform(DsHapticEvent.Confirm))
        }
    }
}
