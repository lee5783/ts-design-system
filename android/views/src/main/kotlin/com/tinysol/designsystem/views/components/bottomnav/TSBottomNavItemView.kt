package com.tinysol.designsystem.views.components.bottomnav

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.textview.MaterialTextView
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.logic.TSBadgeCountFormatter
import com.tinysol.designsystem.core.style.TSBottomNavItem
import com.tinysol.designsystem.core.style.TSBottomNavStyleSpec
import com.tinysol.designsystem.core.style.TSColorRole
import com.tinysol.designsystem.core.style.TSControlState
import com.tinysol.designsystem.views.R
import com.tinysol.designsystem.views.components.shared.TSControlBackground
import com.tinysol.designsystem.views.components.shared.dpToPx
import com.tinysol.designsystem.views.theme.tsColor
import kotlin.math.roundToInt

internal class TSBottomNavItemView(context: Context) : ViewGroup(context) {
    internal val pillView = View(context)
    internal val iconView = AppCompatImageView(context)
    internal val labelView = MaterialTextView(context)
    internal val badgeView = MaterialTextView(context)
    private val pillChrome = TSControlBackground()
    private val badgeShape = GradientDrawable()
    private val focusRingWidth = resources.getDimension(R.dimen.ts_focus_ring_width)
    private val ringOutset = focusRingWidth.toInt() * 2
    private var pillColor = 0
    private var foregroundColor = 0
    private var animator: ValueAnimator? = null
    private val evaluator = ArgbEvaluator()
    private var ready = false

    var reduceMotion: () -> Boolean = { false }

    var item: TSBottomNavItem = TSBottomNavItem("", "", TSIcon.Home)
        set(value) {
            field = value
            applyItem()
        }

    var isItemSelected: Boolean = false
        private set

    init {
        pillView.background = pillChrome
        badgeView.background = badgeShape
        listOf(pillView, iconView, labelView, badgeView).forEach(::addView)
        listOf(pillView, iconView).forEach { it.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }
        labelView.maxLines = 1
        labelView.isSingleLine = true
        labelView.ellipsize = TextUtils.TruncateAt.END
        labelView.gravity = Gravity.CENTER
        labelView.includeFontPadding = false
        labelView.typeface = Typeface.create(MEDIUM_FAMILY, Typeface.NORMAL)
        badgeView.gravity = Gravity.CENTER
        badgeView.includeFontPadding = false
        badgeView.maxLines = 1
        badgeView.typeface = Typeface.create(MEDIUM_FAMILY, Typeface.NORMAL)
        isClickable = true
        isFocusable = true
        ready = true
        applyMetrics()
        applyItem()
        applyState(animated = false)
    }

    internal val currentState: TSControlState
        get() = TSControlState.make(pressed = isPressed, enabled = isEnabled, focused = isFocused)

    internal val resolved: TSBottomNavStyleSpec.Resolved
        get() = TSBottomNavStyleSpec.resolve(TSBottomNavStyleSpec.Configuration(isItemSelected), currentState)

    internal val pillBounds: Rect
        get() = Rect(pillView.left + ringOutset, pillView.top + ringOutset, pillView.right - ringOutset, pillView.bottom - ringOutset)

    internal val pillRingBounds: Rect
        get() = pillChrome.ringBounds.apply { offset(pillView.left, pillView.top) }

    internal val pillFillColor: Int
        get() = pillColor

    fun setItemSelected(selected: Boolean, animated: Boolean) {
        if (selected == isItemSelected) return
        isItemSelected = selected
        isSelected = selected
        applyState(animated)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (ready) applyState(animated = false)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (ready) applyState(animated = false)
    }

    override fun onDetachedFromWindow() {
        animator?.end()
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val resolved = resolved
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = dpToPx(TSBottomNavStyleSpec.container.heightDp)
        val pillWidth = dpToPx(resolved.pillWidthDp)
        val pillHeight = dpToPx(resolved.pillHeightDp)
        val iconSize = dpToPx(resolved.iconSizeDp)
        pillView.measure(exactly(pillWidth + ringOutset * 2), exactly(pillHeight + ringOutset * 2))
        iconView.measure(exactly(iconSize), exactly(iconSize))
        labelView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), exactly(labelLineHeight))
        badgeView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), exactly(dpToPx(TSBottomNavStyleSpec.badge.heightDp)))
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = bottom - top
        val spacing = dpToPx(resolved.spacingDp)
        val pillWidth = pillView.measuredWidth - ringOutset * 2
        val pillHeight = pillView.measuredHeight - ringOutset * 2
        val column = pillHeight + spacing + labelView.measuredHeight
        val pillTop = (height - column) / 2
        val centerX = width / 2
        val pillLeft = centerX - pillWidth / 2
        pillView.layout(pillLeft - ringOutset, pillTop - ringOutset, pillLeft + pillWidth + ringOutset, pillTop + pillHeight + ringOutset)
        val iconLeft = centerX - iconView.measuredWidth / 2
        val iconTop = pillTop + (pillHeight - iconView.measuredHeight) / 2
        iconView.layout(iconLeft, iconTop, iconLeft + iconView.measuredWidth, iconTop + iconView.measuredHeight)
        val labelTop = pillTop + pillHeight + spacing
        val labelLeft = centerX - labelView.measuredWidth / 2
        labelView.layout(labelLeft, labelTop, labelLeft + labelView.measuredWidth, labelTop + labelView.measuredHeight)
        val badge = TSBottomNavStyleSpec.badge
        val badgeTop = pillTop + dpToPx(badge.topFromPillDp)
        val offset = dpToPx(badge.offsetStartDp)
        val badgeWidth = badgeView.measuredWidth
        val badgeLeft = if (layoutDirection == LAYOUT_DIRECTION_RTL) centerX - offset - badgeWidth else centerX + offset
        badgeView.layout(badgeLeft, badgeTop, badgeLeft + badgeWidth, badgeTop + badgeView.measuredHeight)
        pillChrome.setVisualSize(pillWidth, pillHeight)
    }

    private fun exactly(size: Int): Int = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)

    private val labelLineHeight: Int
        get() {
            val scale = TSBottomNavStyleSpec.labelFontScale(resources.configuration.fontScale)
            return (resolved.labelTypeRole.values.lineHeight * resources.displayMetrics.density * scale).roundToInt()
        }

    private fun applyMetrics() {
        val resolved = resolved
        val scale = TSBottomNavStyleSpec.labelFontScale(resources.configuration.fontScale)
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resolved.labelTypeRole.values.fontSize * resources.displayMetrics.density * scale)
        TextViewCompat.setLineHeight(labelView, labelLineHeight)
        val badge = TSBottomNavStyleSpec.badge
        badgeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, badge.fontSizeDp)
        badgeView.minWidth = dpToPx(badge.minWidthDp)
        val padding = dpToPx(badge.horizontalPaddingDp)
        badgeView.setPaddingRelative(padding, 0, padding, 0)
        badgeShape.cornerRadius = dpToPx(badge.cornerRadiusDp).toFloat()
        badgeShape.setColor(tsColor(badge.fill))
        badgeView.setTextColor(tsColor(badge.foreground))
    }

    private fun applyItem() {
        if (!ready) return
        labelView.text = item.label
        iconView.setImageResource(item.icon.drawableRes)
        val text = item.badge?.let { TSBadgeCountFormatter.format(context, it) }
        badgeView.text = text
        badgeView.visibility = if (text == null) GONE else VISIBLE
        requestLayout()
    }

    private fun applyState(animated: Boolean) {
        val resolved = resolved
        val targetPill = resolved.pillFill?.let(::tsColor) ?: 0
        val targetForeground = tsColor(resolved.foreground)
        alpha = resolved.opacity
        animator?.cancel()
        animator = null
        val duration = if (reduceMotion()) 0 else resolved.animationDurationMillis
        if (animated && duration > 0) {
            val startPill = pillColor
            val startForeground = foregroundColor
            val easing = resolved.animationEasing
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                this.duration = duration.toLong()
                interpolator = PathInterpolator(easing.x1, easing.y1, easing.x2, easing.y2)
                addUpdateListener {
                    val fraction = it.animatedFraction
                    paint(blend(startPill, targetPill, fraction), blend(startForeground, targetForeground, fraction), resolved.showsFocusRing)
                }
                start()
            }
        } else {
            paint(targetPill, targetForeground, resolved.showsFocusRing)
        }
    }

    private fun paint(pill: Int, foreground: Int, showsRing: Boolean) {
        pillColor = pill
        foregroundColor = foreground
        pillChrome.update(
            fillColor = pill,
            borderColor = 0,
            borderWidth = 0f,
            cornerRadius = dpToPx(resolved.pillCornerRadiusDp).toFloat(),
            ringColor = tsColor(TSColorRole.Focus),
            ringGapColor = tsColor(TSColorRole.SurfaceCard),
            ringWidth = focusRingWidth,
            showsRing = showsRing,
        )
        ImageViewCompat.setImageTintList(iconView, ColorStateList.valueOf(foreground))
        labelView.setTextColor(foreground)
    }

    private fun blend(from: Int, to: Int, fraction: Float): Int =
        evaluator.evaluate(fraction, from, to) as Int

    private companion object {
        const val MEDIUM_FAMILY = "sans-serif-medium"
    }
}
