package com.tinysol.designsystem.core.haptics

import android.annotation.SuppressLint
import android.os.Build
import android.view.HapticFeedbackConstants

public enum class DsHapticEvent {
    Confirm,
    Reject,
    ToggleOn,
    ToggleOff,
    Selection,
}

public object DsHaptics {
    @SuppressLint("InlinedApi")
    public fun constantFor(event: DsHapticEvent, sdkInt: Int = Build.VERSION.SDK_INT): Int = when (event) {
        DsHapticEvent.Confirm ->
            if (sdkInt >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY
        DsHapticEvent.Reject ->
            if (sdkInt >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS
        DsHapticEvent.ToggleOn ->
            if (sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) HapticFeedbackConstants.TOGGLE_ON else HapticFeedbackConstants.KEYBOARD_TAP
        DsHapticEvent.ToggleOff ->
            if (sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) HapticFeedbackConstants.TOGGLE_OFF else HapticFeedbackConstants.KEYBOARD_TAP
        DsHapticEvent.Selection ->
            if (sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) HapticFeedbackConstants.SEGMENT_TICK else HapticFeedbackConstants.CLOCK_TICK
    }

    public fun toggleEvent(checked: Boolean): DsHapticEvent = if (checked) DsHapticEvent.ToggleOn else DsHapticEvent.ToggleOff
}
