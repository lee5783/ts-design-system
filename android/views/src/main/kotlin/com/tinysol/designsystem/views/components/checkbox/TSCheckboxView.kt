package com.tinysol.designsystem.views.components.checkbox

import android.content.Context
import android.content.res.ColorStateList
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.Gravity
import android.view.View
import android.widget.Checkable
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import androidx.core.widget.ImageViewCompat
import com.google.android.material.textview.MaterialTextView
import com.tinysol.designsystem.core.style.TSCheckboxStyleSpec
import com.tinysol.designsystem.core.style.TSColorRole
import com.tinysol.designsystem.core.style.TSControlState
import com.tinysol.designsystem.views.R
import com.tinysol.designsystem.views.components.shared.TSControlBackground
import com.tinysol.designsystem.views.components.shared.TSViewSavedState
import com.tinysol.designsystem.views.components.shared.dpToPx
import com.tinysol.designsystem.views.components.shared.performToggleHaptic
import com.tinysol.designsystem.views.components.shared.restoreTSViewState
import com.tinysol.designsystem.views.theme.textAppearance
import com.tinysol.designsystem.views.theme.tsColor

public class TSCheckboxView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), Checkable {
    public fun interface OnCheckedChangeListener {
        public fun onCheckedChanged(view: TSCheckboxView, isChecked: Boolean)
    }

    internal val markView = AppCompatImageView(context)
    private val labelView = MaterialTextView(context)
    private val chrome = TSControlBackground()
    private val focusRingWidth = resources.getDimension(R.dimen.ts_focus_ring_width)
    private var checkedState = false
    private var onCheckedChangeListener: OnCheckedChangeListener? = null
    private var ready = false

    public var label: CharSequence
        get() = labelView.text
        set(value) {
            labelView.text = value
        }

    public var isLabelHidden: Boolean = false
        set(value) {
            field = value
            applyConfiguration()
        }

    init {
        orientation = HORIZONTAL
        isClickable = true
        isFocusable = true
        markView.background = chrome
        markView.scaleType = ImageView.ScaleType.FIT_CENTER
        markView.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        labelView.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        addView(markView, LayoutParams(0, 0))
        addView(labelView, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        context.obtainStyledAttributes(attrs, R.styleable.TSCheckboxView, defStyleAttr, 0).apply {
            label = getText(R.styleable.TSCheckboxView_android_text) ?: ""
            checkedState = getBoolean(R.styleable.TSCheckboxView_android_checked, false)
            isLabelHidden = getBoolean(R.styleable.TSCheckboxView_tsLabelHidden, false)
            recycle()
        }
        ready = true
        applyConfiguration()
    }

    override fun isChecked(): Boolean = checkedState

    override fun setChecked(checked: Boolean) {
        if (checkedState == checked) return
        checkedState = checked
        applyState()
    }

    override fun toggle() {
        isChecked = !checkedState
    }

    public fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        onCheckedChangeListener = listener
    }

    override fun performClick(): Boolean {
        if (isEnabled) {
            toggle()
            performToggleHaptic(checkedState)
            onCheckedChangeListener?.onCheckedChanged(this, checkedState)
        }
        return super.performClick()
    }

    override fun onSaveInstanceState(): Parcelable =
        TSViewSavedState(super.onSaveInstanceState(), bundleOf(CHECKED_KEY to checkedState))

    override fun onRestoreInstanceState(state: Parcelable?) {
        restoreTSViewState(state, { super.onRestoreInstanceState(it) }) { isChecked = it.getBoolean(CHECKED_KEY) }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (ready) applyState()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (ready) applyState()
    }

    internal val currentState: TSControlState
        get() = TSControlState.make(pressed = isPressed, enabled = isEnabled, focused = isFocused)

    internal val resolved: TSCheckboxStyleSpec.Resolved
        get() = TSCheckboxStyleSpec.resolve(TSCheckboxStyleSpec.Configuration(checkedState), currentState)

    private fun applyConfiguration() {
        if (!ready) return
        val resolved = resolved
        val markSize = dpToPx(resolved.markSizeDp)
        markView.layoutParams = (markView.layoutParams as LayoutParams).apply {
            width = markSize
            height = markSize
            marginEnd = if (isLabelHidden) 0 else dpToPx(resolved.spacingDp)
        }
        val inset = (markSize - dpToPx(resolved.checkSizeDp)) / 2
        markView.setPadding(inset, inset, inset, inset)
        labelView.setTextAppearance(resolved.labelTypeRole.textAppearance)
        labelView.setTextColor(tsColor(resolved.labelColor))
        labelView.visibility = if (isLabelHidden) GONE else VISIBLE
        val minTouchTarget = dpToPx(resolved.minTouchTargetDp)
        minimumHeight = minTouchTarget
        minimumWidth = if (isLabelHidden) minTouchTarget else 0
        gravity = if (isLabelHidden) Gravity.CENTER else Gravity.CENTER_VERTICAL or Gravity.START
        applyState()
    }

    private fun applyState() {
        if (!ready) return
        val resolved = resolved
        chrome.update(
            fillColor = tsColor(resolved.markFill),
            borderColor = tsColor(resolved.markBorder),
            borderWidth = resolved.markBorderWidthDp * resources.displayMetrics.density,
            cornerRadius = dpToPx(resolved.markCornerRadiusDp).toFloat(),
            ringColor = tsColor(TSColorRole.Focus),
            ringGapColor = tsColor(TSColorRole.BackgroundPrimary),
            ringWidth = focusRingWidth,
            showsRing = resolved.showsFocusRing,
        )
        if (resolved.showsCheck) {
            markView.setImageResource(resolved.checkIcon.drawableRes)
            ImageViewCompat.setImageTintList(markView, ColorStateList.valueOf(tsColor(resolved.checkColor)))
        } else {
            markView.setImageDrawable(null)
        }
        alpha = resolved.opacity
    }

    private companion object {
        const val CHECKED_KEY = "ts.checked"
    }
}
