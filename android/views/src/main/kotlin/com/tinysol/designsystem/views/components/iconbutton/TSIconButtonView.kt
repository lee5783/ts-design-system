package com.tinysol.designsystem.views.components.iconbutton

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.TooltipCompat
import androidx.core.widget.ImageViewCompat
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.icon.TSIcons
import com.tinysol.designsystem.core.style.TSColorRole
import com.tinysol.designsystem.core.style.TSControlState
import com.tinysol.designsystem.core.style.TSIconButtonStyleSpec
import com.tinysol.designsystem.core.style.TSIconButtonVariant
import com.tinysol.designsystem.views.R
import com.tinysol.designsystem.views.components.shared.TSControlBackground
import com.tinysol.designsystem.views.components.shared.TSIconSlotDrawable
import com.tinysol.designsystem.views.components.shared.dpToPx
import com.tinysol.designsystem.views.components.shared.expandToTouchTarget
import com.tinysol.designsystem.views.theme.tsColor

public class TSIconButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageButton(context, attrs, defStyleAttr) {
    private val chrome = TSControlBackground()
    private val minTouchTarget = resources.getDimensionPixelSize(R.dimen.ts_min_touch_target)
    private val focusRingWidth = resources.getDimension(R.dimen.ts_focus_ring_width)
    private var ready = false

    public var icon: TSIcon? = null
        set(value) {
            field = value
            applyIcon()
        }

    public var iconDrawable: Drawable? = null
        set(value) {
            field = value
            applyIcon()
        }

    public var label: CharSequence?
        get() = contentDescription
        set(value) {
            contentDescription = value
            TooltipCompat.setTooltipText(this, value)
        }

    public var variant: TSIconButtonVariant = TSIconButtonVariant.Ghost
        set(value) {
            field = value
            applyState()
        }

    public var isRound: Boolean = false
        set(value) {
            field = value
            applyState()
        }

    internal var isDimmedByContainer: Boolean = false
        set(value) {
            field = value
            applyState()
        }

    init {
        background = chrome
        scaleType = ImageView.ScaleType.CENTER
        setPadding(0, 0, 0, 0)
        context.obtainStyledAttributes(attrs, R.styleable.TSIconButtonView, defStyleAttr, 0).apply {
            label = getText(R.styleable.TSIconButtonView_android_contentDescription)
            icon = getString(R.styleable.TSIconButtonView_tsIcon)?.let(TSIcons::fromDsName)
            if (hasValue(R.styleable.TSIconButtonView_tsVariant)) {
                variant = variantFor(getInt(R.styleable.TSIconButtonView_tsVariant, 0))
            }
            isRound = getBoolean(R.styleable.TSIconButtonView_tsRound, false)
            recycle()
        }
        ready = true
        applyState()
    }

    public fun setIconResource(@DrawableRes resId: Int) {
        iconDrawable = AppCompatResources.getDrawable(context, resId)
    }

    internal val currentState: TSControlState
        get() = TSControlState.make(pressed = isPressed, enabled = isEnabled, focused = isFocused)

    internal val resolved: TSIconButtonStyleSpec.Resolved
        get() = TSIconButtonStyleSpec.resolve(TSIconButtonStyleSpec.Configuration(variant, isRound), currentState)

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
        chrome.setBounds(0, 0, w, h)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val visual = dpToPx(resolved.sizeDp)
        chrome.setVisualSize(visual, visual)
        setMeasuredDimension(
            expandToTouchTarget(visual, widthMeasureSpec, minTouchTarget),
            expandToTouchTarget(visual, heightMeasureSpec, minTouchTarget),
        )
    }

    private fun applyState() {
        if (!ready) return
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
        ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(tsColor(resolved.icon)))
        colorFilter = PorterDuffColorFilter(tsColor(resolved.icon), PorterDuff.Mode.SRC_IN).takeIf { iconDrawable != null }
        alpha = if (isDimmedByContainer) 1f else resolved.opacity
    }

    private fun applyIcon() {
        val drawable = iconDrawable
        val tsIcon = icon
        when {
            drawable != null -> setImageDrawable(TSIconSlotDrawable(drawable, dpToPx(resolved.iconSizeDp)))
            tsIcon != null -> setImageResource(tsIcon.drawableRes)
            else -> setImageDrawable(null)
        }
        applyState()
    }

    private fun variantFor(value: Int): TSIconButtonVariant = when (value) {
        0 -> TSIconButtonVariant.Primary
        1 -> TSIconButtonVariant.Secondary
        2 -> TSIconButtonVariant.Ghost
        else -> error("tsVariant ${VARIANT_NAMES.getOrNull(value)} is not an icon button variant")
    }

    private companion object {
        val VARIANT_NAMES = listOf("primary", "secondary", "ghost", "danger", "link")
    }
}
