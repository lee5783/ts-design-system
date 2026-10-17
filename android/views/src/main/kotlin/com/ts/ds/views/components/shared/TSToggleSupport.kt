package com.ts.ds.views.components.shared

import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.ts.ds.core.haptics.DsHaptics

internal class TSToggleAccessibilityInfo(
    private val className: String,
    private val label: () -> CharSequence,
    private val checked: () -> Boolean,
) {
    fun populate(info: AccessibilityNodeInfo) {
        val node = AccessibilityNodeInfoCompat.wrap(info)
        node.className = className
        node.isCheckable = true
        node.isChecked = checked()
        node.text = label()
    }

    fun populate(event: AccessibilityEvent) {
        event.className = className
        event.isChecked = checked()
    }
}

internal fun View.performToggleHaptic(checked: Boolean): Boolean =
    performHapticFeedback(DsHaptics.constantFor(DsHaptics.toggleEvent(checked)))
