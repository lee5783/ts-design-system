package com.tinysol.designsystem.views.components.button

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import com.google.android.material.textview.MaterialTextView
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.icon.TSIcons
import com.tinysol.designsystem.core.style.TSButtonSize
import com.tinysol.designsystem.core.style.TSButtonStyleSpec
import com.tinysol.designsystem.core.style.TSButtonVariant
import com.tinysol.designsystem.core.style.TSColorRole
import com.tinysol.designsystem.core.style.TSControlState
import com.tinysol.designsystem.views.R
import com.tinysol.designsystem.views.components.shared.TSControlBackground
import com.tinysol.designsystem.views.components.shared.TSSpinnerDrawable
import com.tinysol.designsystem.views.components.shared.dpToPx
import com.tinysol.designsystem.views.components.shared.expandToTouchTarget
import com.tinysol.designsystem.views.theme.textAppearance
import com.tinysol.designsystem.views.theme.tsColor

public class TSButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    private val leadingView = AppCompatImageView(context)
    private val labelView = MaterialTextView(context)
    private val trailingView = AppCompatImageView(context)
    private val chrome = TSControlBackground()
    private val spinner: TSSpinnerDrawable
    private val minTouchTarget = resources.getDimensionPixelSize(R.dimen.ts_min_touch_target)
    private val focusRingWidth = resources.getDimension(R.dimen.ts_focus_ring_width)
    private var userEnabled = true
    private var ready = false

    public var text: CharSequence
        get() = labelView.text
        set(value) {
            labelView.text = value
        }

    public var variant: TSButtonVariant = TSButtonVariant.Primary
        set(value) {
            field = value
            applyConfiguration()
        }

    public var size: TSButtonSize = TSButtonSize.Md
        set(value) {
            field = value
            applyConfiguration()
        }

    internal var isDimmedByContainer: Boolean = false
        set(value) {
            field = value
            applyState()
        }

    public var leadingIcon: TSIcon? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var trailingIcon: TSIcon? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var leadingIconDrawable: Drawable? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var trailingIconDrawable: Drawable? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var maxLines: Int
        get() = labelView.maxLines
        set(value) {
            labelView.maxLines = value
        }

    public var ellipsize: TextUtils.TruncateAt?
        get() = labelView.ellipsize
        set(value) {
            labelView.ellipsize = value
        }

    public var isLoading: Boolean = false
        set(value) {
            field = value
            super.setEnabled(userEnabled && !value)
            applyConfiguration()
        }

    init {
        val initial = TSButtonStyleSpec.resolve(TSButtonStyleSpec.Configuration(), TSControlState.Default)
        spinner = TSSpinnerDrawable(
            size = dpToPx(initial.spinnerSizeDp),
            slotSize = dpToPx(initial.iconSizeDp),
            strokeWidth = dpToPx(initial.spinnerStrokeWidthDp).toFloat(),
            rotationMillis = initial.spinnerRotationMillis.toLong(),
        )
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true
        background = chrome
        val iconSize = dpToPx(initial.iconSizeDp)
        listOf(leadingView, trailingView).forEach { it.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }
        addView(leadingView, LayoutParams(iconSize, iconSize).apply { marginEnd = dpToPx(initial.iconSpacingDp) })
        addView(labelView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(trailingView, LayoutParams(iconSize, iconSize).apply { marginStart = dpToPx(initial.iconSpacingDp) })
        labelView.gravity = Gravity.CENTER
        context.obtainStyledAttributes(attrs, R.styleable.TSButtonView, defStyleAttr, 0).apply {
            text = getText(R.styleable.TSButtonView_android_text) ?: ""
            variant = TSButtonVariant.entries[getInt(R.styleable.TSButtonView_tsVariant, TSButtonVariant.Primary.ordinal)]
            size = TSButtonSize.entries[getInt(R.styleable.TSButtonView_tsSize, TSButtonSize.Md.ordinal)]
            leadingIcon = getString(R.styleable.TSButtonView_tsLeadingIcon)?.let(TSIcons::fromDsName)
            trailingIcon = getString(R.styleable.TSButtonView_tsTrailingIcon)?.let(TSIcons::fromDsName)
            userEnabled = isEnabled
            isLoading = getBoolean(R.styleable.TSButtonView_tsLoading, false)
            maxLines = getInt(R.styleable.TSButtonView_android_maxLines, Int.MAX_VALUE)
            ellipsize = ELLIPSIZE.getOrNull(getInt(R.styleable.TSButtonView_android_ellipsize, -1) - 1)
            recycle()
        }
        ready = true
        applyConfiguration()
    }

    public fun setLeadingIconResource(@DrawableRes resId: Int) {
        leadingIconDrawable = AppCompatResources.getDrawable(context, resId)
    }

    public fun setTrailingIconResource(@DrawableRes resId: Int) {
        trailingIconDrawable = AppCompatResources.getDrawable(context, resId)
    }

    override fun setEnabled(enabled: Boolean) {
        userEnabled = enabled
        super.setEnabled(enabled && !isLoading)
        if (ready) applyState()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (ready) applyState()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (ready && isLoading) spinner.start()
    }

    override fun onDetachedFromWindow() {
        spinner.stop()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        chrome.setBounds(0, 0, w, h)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val visualWidth = measuredWidth
        val visualHeight = measuredHeight
        chrome.setVisualSize(visualWidth, visualHeight)
        setMeasuredDimension(
            expandToTouchTarget(visualWidth, widthMeasureSpec, minTouchTarget),
            expandToTouchTarget(visualHeight, heightMeasureSpec, minTouchTarget),
        )
    }

    internal val currentState: TSControlState
        get() = TSControlState.make(pressed = isPressed, enabled = userEnabled, loading = isLoading, focused = isFocused)

    internal val resolved: TSButtonStyleSpec.Resolved
        get() = TSButtonStyleSpec.resolve(TSButtonStyleSpec.Configuration(variant, size), currentState)

    private fun applyConfiguration() {
        if (!ready) return
        val resolved = resolved
        val labelColor = ColorStateList.valueOf(tsColor(resolved.label))
        labelView.setTextAppearance(resolved.typeRole.textAppearance)
        labelView.typeface = Typeface.create(MEDIUM_FAMILY, Typeface.NORMAL)
        labelView.setTextColor(labelColor)
        labelView.paintFlags = if (resolved.underlined) {
            labelView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        } else {
            labelView.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        }
        val horizontal = dpToPx(resolved.horizontalPaddingDp)
        val vertical = dpToPx(resolved.verticalPaddingDp)
        setPaddingRelative(horizontal, vertical, horizontal, vertical)
        minimumHeight = dpToPx(resolved.minHeightDp)
        elevation = resolved.shadow?.let { dpToPx(it.values.elevation).toFloat() } ?: 0f
        spinner.color = labelColor.defaultColor
        when {
            resolved.showsSpinner -> {
                leadingView.setImageDrawable(spinner)
                leadingView.visibility = VISIBLE
                if (isAttachedToWindow) spinner.start()
            }
            leadingIconDrawable != null -> {
                spinner.stop()
                leadingView.setImageDrawable(leadingIconDrawable)
                leadingView.visibility = VISIBLE
            }
            leadingIcon != null -> {
                spinner.stop()
                leadingView.setImageResource(requireNotNull(leadingIcon).drawableRes)
                leadingView.visibility = VISIBLE
            }
            else -> {
                spinner.stop()
                leadingView.setImageDrawable(null)
                leadingView.visibility = GONE
            }
        }
        val trailingDrawable = trailingIconDrawable
        val trailing = trailingIcon
        trailingView.visibility = if (trailingDrawable == null && trailing == null) GONE else VISIBLE
        when {
            trailingDrawable != null -> trailingView.setImageDrawable(trailingDrawable)
            trailing != null -> trailingView.setImageResource(trailing.drawableRes)
        }
        listOf(leadingView, trailingView).forEach { ImageViewCompat.setImageTintList(it, labelColor) }
        val consumerTint = PorterDuffColorFilter(labelColor.defaultColor, PorterDuff.Mode.SRC_IN)
        leadingView.colorFilter = consumerTint.takeIf { leadingIconDrawable != null && !resolved.showsSpinner }
        trailingView.colorFilter = consumerTint.takeIf { trailingDrawable != null }
        applyState()
    }

    private fun applyState() {
        val resolved = resolved
        chrome.update(
            fillColor = resolved.fill?.let(::tsColor) ?: 0,
            borderColor = resolved.border?.let(::tsColor) ?: 0,
            borderWidth = dpToPx(resolved.borderWidthDp).toFloat(),
            cornerRadius = dpToPx(resolved.cornerRadiusDp).toFloat(),
            ringColor = tsColor(TSColorRole.Focus),
            ringGapColor = tsColor(TSColorRole.BackgroundPrimary),
            ringWidth = focusRingWidth,
            showsRing = resolved.showsFocusRing,
        )
        alpha = if (isDimmedByContainer) 1f else resolved.opacity
    }

    private companion object {
        const val MEDIUM_FAMILY = "sans-serif-medium"
        val ELLIPSIZE = listOf(TextUtils.TruncateAt.START, TextUtils.TruncateAt.MIDDLE, TextUtils.TruncateAt.END, TextUtils.TruncateAt.MARQUEE)
    }
}
