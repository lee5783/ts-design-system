package com.ts.ds.views.components.shared

import android.view.View
import com.ts.ds.core.haptics.DsHaptics

internal fun View.performToggleHaptic(checked: Boolean): Boolean =
    performHapticFeedback(DsHaptics.constantFor(DsHaptics.toggleEvent(checked)))
