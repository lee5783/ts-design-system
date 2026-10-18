package com.ts.ds.views.components.tabs

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import com.google.android.material.textview.MaterialTextView
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSControlState
import com.ts.ds.core.style.TSTabItem
import com.ts.ds.core.style.TSTabsStyleSpec
import com.ts.ds.core.style.TSTabsVariant
import com.ts.ds.views.R
import com.ts.ds.views.components.shared.TSControlBackground
import com.ts.ds.views.components.shared.dpToPx
import com.ts.ds.views.theme.textAppearance
import com.ts.ds.views.theme.tsColor

internal class TSTabView(context: Context) : MaterialTextView(context) {
    private val chrome = TSControlBackground()
    private val focusRingWidth = resources.getDimension(R.dimen.ts_focus_ring_width)
    private var ready = false

    var item: TSTabItem = TSTabItem("", "")
        set(value) {
            field = value
            text = value.label
        }

    var variant: TSTabsVariant = TSTabsVariant.Underline
        set(value) {
            field = value
            applyConfiguration()
        }

    var isTabSelected: Boolean = false
        set(value) {
            field = value
            isSelected = value
            applyConfiguration()
        }


    init {
        background = chrome
        gravity = Gravity.CENTER
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        isClickable = true
        isFocusable = true
        ready = true
        applyConfiguration()
    }

    internal val currentState: TSControlState
        get() = TSControlState.make(pressed = isPressed, enabled = isEnabled, focused = isFocused)

    internal val resolved: TSTabsStyleSpec.Resolved
        get() = TSTabsStyleSpec.resolve(TSTabsStyleSpec.Configuration(variant, isTabSelected), currentState)

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (ready) applyState()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (ready) applyState()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        chrome.setVisualSize(w, h - dpToPx(resolved.touchOutsetDp) * 2)
    }

    private fun applyConfiguration() {
        if (!ready) return
        val resolved = resolved
        setTextAppearance(resolved.labelTypeRole.textAppearance)
        val vertical = dpToPx(resolved.touchOutsetDp + resolved.paddingVerticalDp)
        val horizontal = dpToPx(resolved.paddingHorizontalDp)
        setPaddingRelative(horizontal, vertical, horizontal, vertical)
        minHeight = dpToPx(resolved.minTouchTargetDp)
        chrome.setVisualSize(width, height - dpToPx(resolved.touchOutsetDp) * 2)
        applyState()
    }

    private fun applyState() {
        if (!ready) return
        val resolved = resolved
        setTextColor(tsColor(resolved.labelColor))
        chrome.update(
            fillColor = if (isTabSelected) 0 else resolved.fill?.let(::tsColor) ?: 0,
            borderColor = 0,
            borderWidth = 0f,
            cornerRadius = dpToPx(resolved.cornerRadiusDp).toFloat(),
            ringColor = tsColor(TSColorRole.Focus),
            ringGapColor = tsColor(TSColorRole.BackgroundPrimary),
            ringWidth = focusRingWidth,
            showsRing = resolved.showsFocusRing,
        )
    }
}
