package com.ts.ds.compose.components.switchcontrol

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.assertTouchWidthIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.components.shared.LocalTSReduceMotion
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.haptics.DsHapticEvent
import com.ts.ds.core.haptics.DsHaptics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSSwitchTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val changes = mutableListOf<Boolean>()
    private lateinit var hostView: View

    private fun setSwitch(
        initial: Boolean = false,
        enabled: Boolean = true,
        labelHidden: Boolean = false,
        direction: LayoutDirection = LayoutDirection.Ltr,
        reduceMotion: Boolean = false,
    ) {
        composeRule.setContent {
            hostView = LocalView.current
            CompositionLocalProvider(LocalLayoutDirection provides direction, LocalTSReduceMotion provides reduceMotion) {
                TSTheme(darkTheme = false) {
                    var checked by remember { mutableStateOf(initial) }
                    Box(Modifier.testTag("container")) {
                        TSSwitch(
                            checked = checked,
                            label = "Wi-Fi",
                            onCheckedChange = {
                                changes += it
                                checked = it
                            },
                            labelHidden = labelHidden,
                            enabled = enabled,
                            modifier = Modifier.testTag("switch"),
                        )
                    }
                }
            }
        }
    }

    private fun thumbOffsetFromTrackLeft(): Dp {
        val track = composeRule.onNodeWithTag(TSSwitchTestTags.TRACK, useUnmergedTree = true).getBoundsInRoot()
        val thumb = composeRule.onNodeWithTag(TSSwitchTestTags.THUMB, useUnmergedTree = true).getBoundsInRoot()
        return thumb.left - track.left
    }

    private fun assertOffset(expected: Dp) {
        assertEquals(expected.value, thumbOffsetFromTrackLeft().value, 0.5f)
    }

    @Test
    fun tapOnTheLabelToggles() {
        setSwitch()

        composeRule.onNodeWithTag("switch").assertIsOff()
        composeRule.onNodeWithText("Wi-Fi").performClick()
        composeRule.onNodeWithTag("switch").assertIsOn()

        assertEquals(listOf(true), changes)
        assertEquals(DsHaptics.constantFor(DsHapticEvent.ToggleOn), shadowOf(hostView).lastHapticFeedbackPerformed())
    }

    @Test
    fun disabledBlocksToggling() {
        setSwitch(enabled = false)

        composeRule.onNodeWithTag("switch").assertIsNotEnabled().performClick()

        assertEquals(emptyList<Boolean>(), changes)
    }

    @Test
    fun trackIs44By26AndTheRowIs48dpTall() {
        setSwitch()

        composeRule.onNodeWithTag(TSSwitchTestTags.TRACK, useUnmergedTree = true).assertWidthIsEqualTo(44.dp).assertHeightIsEqualTo(26.dp)
        composeRule.onNodeWithTag(TSSwitchTestTags.THUMB, useUnmergedTree = true).assertWidthIsEqualTo(20.dp).assertHeightIsEqualTo(20.dp)
        composeRule.onNodeWithTag("switch").assertHeightIsEqualTo(48.dp)
    }

    @Test
    fun hiddenLabelIsA48dpTarget() {
        setSwitch(labelHidden = true)

        composeRule.onNodeWithTag("container").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(48.dp)
        composeRule.onNodeWithTag("switch").assertTouchWidthIsEqualTo(48.dp).assertTouchHeightIsEqualTo(48.dp)
    }

    @Test
    fun onIsTheTrailingSideInLeftToRight() {
        setSwitch()
        assertOffset(3.dp)

        composeRule.onNodeWithTag("switch").performClick()
        composeRule.waitForIdle()

        assertOffset(21.dp)
    }

    @Test
    fun onIsTheTrailingSideInRightToLeft() {
        setSwitch(direction = LayoutDirection.Rtl)
        assertOffset(21.dp)

        composeRule.onNodeWithTag("switch").performClick()
        composeRule.waitForIdle()

        assertOffset(3.dp)
    }

    @Test
    fun thumbAnimatesOver150Milliseconds() {
        setSwitch()
        composeRule.mainClock.autoAdvance = false

        composeRule.onNodeWithTag("switch").performClick()
        composeRule.mainClock.advanceTimeBy(80)
        val midway = thumbOffsetFromTrackLeft().value
        assertTrue("thumb at $midway dp is between the ends", midway > 3.5f && midway < 20.5f)

        composeRule.mainClock.advanceTimeBy(200)
        assertOffset(21.dp)
    }

    @Test
    fun reduceMotionMovesTheThumbImmediately() {
        setSwitch(reduceMotion = true)
        composeRule.mainClock.autoAdvance = false

        composeRule.onNodeWithTag("switch").performClick()
        composeRule.mainClock.advanceTimeBy(48)

        assertOffset(21.dp)
    }
}
