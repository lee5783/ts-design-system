package com.ts.ds.core.haptics

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class DsHapticsTest {
    private fun table(): Map<DsHapticEvent, Int> = DsHapticEvent.entries.associateWith { DsHaptics.constantFor(it) }

    @Test
    @Config(sdk = [26])
    fun api26FallsBackToLegacyConstants() {
        assertEquals(26, Build.VERSION.SDK_INT)
        assertEquals(
            mapOf(
                DsHapticEvent.Confirm to HapticFeedbackConstants.VIRTUAL_KEY,
                DsHapticEvent.Reject to HapticFeedbackConstants.LONG_PRESS,
                DsHapticEvent.ToggleOn to HapticFeedbackConstants.KEYBOARD_TAP,
                DsHapticEvent.ToggleOff to HapticFeedbackConstants.KEYBOARD_TAP,
                DsHapticEvent.Selection to HapticFeedbackConstants.CLOCK_TICK,
            ),
            table(),
        )
    }

    @Test
    @Config(sdk = [30])
    fun api30UsesConfirmAndReject() {
        assertEquals(30, Build.VERSION.SDK_INT)
        assertEquals(
            mapOf(
                DsHapticEvent.Confirm to HapticFeedbackConstants.CONFIRM,
                DsHapticEvent.Reject to HapticFeedbackConstants.REJECT,
                DsHapticEvent.ToggleOn to HapticFeedbackConstants.KEYBOARD_TAP,
                DsHapticEvent.ToggleOff to HapticFeedbackConstants.KEYBOARD_TAP,
                DsHapticEvent.Selection to HapticFeedbackConstants.CLOCK_TICK,
            ),
            table(),
        )
    }

    @Test
    @Config(sdk = [34])
    fun api34UsesToggleAndSegmentConstants() {
        assertEquals(34, Build.VERSION.SDK_INT)
        assertEquals(
            mapOf(
                DsHapticEvent.Confirm to HapticFeedbackConstants.CONFIRM,
                DsHapticEvent.Reject to HapticFeedbackConstants.REJECT,
                DsHapticEvent.ToggleOn to HapticFeedbackConstants.TOGGLE_ON,
                DsHapticEvent.ToggleOff to HapticFeedbackConstants.TOGGLE_OFF,
                DsHapticEvent.Selection to HapticFeedbackConstants.SEGMENT_TICK,
            ),
            table(),
        )
    }

    @Test
    fun explicitSdkBoundaries() {
        assertEquals(HapticFeedbackConstants.VIRTUAL_KEY, DsHaptics.constantFor(DsHapticEvent.Confirm, sdkInt = 29))
        assertEquals(HapticFeedbackConstants.KEYBOARD_TAP, DsHaptics.constantFor(DsHapticEvent.ToggleOn, sdkInt = 33))
        assertEquals(HapticFeedbackConstants.TOGGLE_ON, DsHaptics.constantFor(DsHapticEvent.ToggleOn, sdkInt = 36))
    }

    @Test
    @Config(sdk = [34])
    fun toggleEventFollowsTheNewValue() {
        assertEquals(DsHapticEvent.ToggleOn, DsHaptics.toggleEvent(checked = true))
        assertEquals(DsHapticEvent.ToggleOff, DsHaptics.toggleEvent(checked = false))
    }
}
