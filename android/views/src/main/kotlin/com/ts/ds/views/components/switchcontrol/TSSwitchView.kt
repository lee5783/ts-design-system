package com.ts.ds.views.components.switchcontrol

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.PathInterpolator
import android.widget.Checkable
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.core.graphics.ColorUtils
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textview.MaterialTextView
import com.ts.ds.core.logic.TSToggleAccessibility
import com.ts.ds.core.motion.TSReduceMotion
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSControlState
import com.ts.ds.core.style.TSSwitchStyleSpec
import com.ts.ds.views.R
import com.ts.ds.views.components.shared.TSControlBackground
import com.ts.ds.views.components.shared.TSToggleAccessibilityInfo
import com.ts.ds.views.components.shared.TSViewSavedState
import com.ts.ds.views.components.shared.dpToPx
import com.ts.ds.views.components.shared.performToggleHaptic
import com.ts.ds.views.components.shared.restoreTSViewState
import com.ts.ds.views.theme.textAppearance
import com.ts.ds.views.theme.tsColor
import kotlin.math.roundToInt

public class TSSwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), Checkable {
    public fun interface OnCheckedChangeListener {
        public fun onCheckedChanged(view: TSSwitchView, isChecked: Boolean)
    }

    internal val trackView = FrameLayout(context)
    internal val thumbView = View(context)
    private val labelView = MaterialTextView(context)
    private val chrome = TSControlBackground()
    private val thumbFill = MaterialShapeDrawable(ShapeAppearanceModel.builder().setAllCornerSizes(ShapeAppearanceModel.PILL).build())
    private val focusRingWidth = resources.getDimension(R.dimen.ts_focus_ring_width)
    private val argbEvaluator = ArgbEvaluator()
    private var checkedState = false
    private val toggleAccessibility = TSToggleAccessibilityInfo(SwitchCompat::class.java.name, { label }, { checkedState })
    private var progress = 0f
    private var animator: ValueAnimator? = null
    private var onCheckedChangeListener: OnCheckedChangeListener? = null
    private var ready = false

    internal var reduceMotion: () -> Boolean = { TSReduceMotion.isEnabled }

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
        trackView.background = chrome
        trackView.clipChildren = false
        trackView.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        thumbView.background = thumbFill
        trackView.addView(thumbView, FrameLayout.LayoutParams(0, 0, Gravity.LEFT or Gravity.CENTER_VERTICAL))
        labelView.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        labelView.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        addView(trackView, LayoutParams(0, 0))
        addView(labelView, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        context.obtainStyledAttributes(attrs, R.styleable.TSSwitchView, defStyleAttr, 0).apply {
            label = getText(R.styleable.TSSwitchView_android_text) ?: ""
            checkedState = getBoolean(R.styleable.TSSwitchView_android_checked, false)
            progress = if (checkedState) 1f else 0f
            isLabelHidden = getBoolean(R.styleable.TSSwitchView_tsLabelHidden, false)
            recycle()
        }
        ready = true
        applyConfiguration()
    }

    override fun isChecked(): Boolean = checkedState

    override fun setChecked(checked: Boolean) {
        setChecked(checked, animated = false)
    }

    public fun setChecked(checked: Boolean, animated: Boolean) {
        if (checkedState == checked) return
        checkedState = checked
        animateProgress(if (checked) 1f else 0f, animated)
    }

    override fun toggle() {
        isChecked = !checkedState
    }

    public fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        onCheckedChangeListener = listener
    }

    override fun performClick(): Boolean {
        if (isEnabled) {
            setChecked(!checkedState, animated = true)
            performToggleHaptic(checkedState)
            onCheckedChangeListener?.onCheckedChanged(this, checkedState)
        }
        return super.performClick()
    }

    override fun onSaveInstanceState(): Parcelable =
        TSViewSavedState(super.onSaveInstanceState(), bundleOf(CHECKED_KEY to checkedState))

    override fun onRestoreInstanceState(state: Parcelable?) {
        restoreTSViewState(state, { super.onRestoreInstanceState(it) }) { setChecked(it.getBoolean(CHECKED_KEY), animated = false) }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        toggleAccessibility.populate(info)
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        toggleAccessibility.populate(event)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (ready) applyState()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (ready) applyState()
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        if (ready) applyState()
    }

    override fun onDetachedFromWindow() {
        animator?.end()
        super.onDetachedFromWindow()
    }

    internal val currentState: TSControlState
        get() = TSControlState.make(pressed = isPressed, enabled = isEnabled, focused = isFocused)

    internal val resolved: TSSwitchStyleSpec.Resolved
        get() = TSSwitchStyleSpec.resolve(TSSwitchStyleSpec.Configuration(checkedState), currentState)

    internal val thumbProgress: Float
        get() = progress

    internal val runningAnimator: ValueAnimator?
        get() = animator?.takeIf { it.isRunning || it.isStarted }

    private fun animateProgress(target: Float, animated: Boolean) {
        animator?.cancel()
        animator = null
        val duration = TSReduceMotion.durationMillis(resolved.animationDurationMillis, reduceMotion())
        if (!animated || duration == 0 || !ready) {
            progress = target
            applyState()
            return
        }
        val easing = resolved.animationEasing
        animator = ValueAnimator.ofFloat(progress, target).apply {
            this.duration = duration.toLong()
            interpolator = PathInterpolator(easing.x1, easing.y1, easing.x2, easing.y2)
            addUpdateListener {
                progress = it.animatedValue as Float
                applyState()
            }
            start()
        }
    }

    private fun applyConfiguration() {
        if (!ready) return
        val resolved = resolved
        trackView.layoutParams = (trackView.layoutParams as LayoutParams).apply {
            width = dpToPx(resolved.trackWidthDp)
            height = dpToPx(resolved.trackHeightDp)
            marginEnd = if (isLabelHidden) 0 else dpToPx(resolved.spacingDp)
        }
        val thumbSize = dpToPx(resolved.thumbSizeDp)
        thumbView.layoutParams = (thumbView.layoutParams as FrameLayout.LayoutParams).apply {
            width = thumbSize
            height = thumbSize
        }
        thumbView.elevation = dpToPx(resolved.thumbShadow.values.elevation).toFloat()
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
        val state = currentState
        val resolved = TSSwitchStyleSpec.resolve(TSSwitchStyleSpec.Configuration(checkedState), state)
        val off = TSSwitchStyleSpec.resolve(TSSwitchStyleSpec.Configuration(checked = false), state)
        val on = TSSwitchStyleSpec.resolve(TSSwitchStyleSpec.Configuration(checked = true), state)
        val trackColor = argbEvaluator.evaluate(progress, trackColor(off), trackColor(on)) as Int
        chrome.update(
            fillColor = trackColor,
            borderColor = 0,
            borderWidth = 0f,
            cornerRadius = dpToPx(resolved.trackCornerRadiusDp).toFloat(),
            ringColor = tsColor(TSColorRole.Focus),
            ringGapColor = tsColor(TSColorRole.BackgroundPrimary),
            ringWidth = focusRingWidth,
            showsRing = resolved.showsFocusRing,
        )
        thumbFill.fillColor = ColorStateList.valueOf(tsColor(resolved.thumbColor))
        val rightToLeft = layoutDirection == LAYOUT_DIRECTION_RTL
        val offLeft = off.thumbLeftDp(rightToLeft)
        val onLeft = on.thumbLeftDp(rightToLeft)
        thumbView.translationX = dpToPx(offLeft + (onLeft - offLeft) * progress).toFloat()
        alpha = resolved.opacity
        ViewCompat.setStateDescription(this, TSToggleAccessibility.switchState(context, checkedState))
    }

    private fun trackColor(resolved: TSSwitchStyleSpec.Resolved): Int =
        ColorUtils.setAlphaComponent(tsColor(resolved.trackColor), (resolved.trackOpacity * 255).roundToInt())

    private companion object {
        const val CHECKED_KEY = "ts.checked"
    }
}
