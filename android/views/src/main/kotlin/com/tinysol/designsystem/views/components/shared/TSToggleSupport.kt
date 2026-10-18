package com.tinysol.designsystem.views.components.shared

import android.view.View
import com.tinysol.designsystem.core.haptics.DsHaptics

internal fun View.performToggleHaptic(checked: Boolean): Boolean =
    performHapticFeedback(DsHaptics.constantFor(DsHaptics.toggleEvent(checked)))
