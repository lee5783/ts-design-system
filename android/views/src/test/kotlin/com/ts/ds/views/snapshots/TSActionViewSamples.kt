package com.ts.ds.views.snapshots

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSButtonSize
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSIconButtonVariant
import com.ts.ds.core.style.TSIconSize
import com.ts.ds.views.R
import com.ts.ds.views.components.button.TSButtonView
import com.ts.ds.views.components.iconbutton.TSIconButtonView
import com.ts.ds.views.theme.tsColor

internal enum class SampleState { Default, Pressed, Disabled, Loading }

internal fun Context.dp(value: Float): Int = (value * resources.displayMetrics.density).toInt()

internal fun Context.galleryColumn(): LinearLayout = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    setBackgroundColor(tsColor(R.attr.tsColorBackgroundPrimary))
    val padding = dp(16f)
    setPadding(padding, padding, padding, padding)
    showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
    dividerDrawable = GradientDrawable().apply { setSize(0, dp(4f)) }
}

internal fun Context.galleryRow(): LinearLayout = LinearLayout(this).apply {
    orientation = LinearLayout.HORIZONTAL
    showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
    dividerDrawable = GradientDrawable().apply { setSize(dp(8f), 0) }
}

private val wrap = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

private fun View.focusWhenAttached() {
    isFocusableInTouchMode = true
    post { requestFocus() }
}

internal fun buttonGallerySample(context: Context, variant: TSButtonVariant): View = context.galleryColumn().apply {
    TSButtonSize.entries.forEach { size ->
        SampleState.entries.forEach { state ->
            addView(
                TSButtonView(context).apply {
                    text = "${size.name} ${state.name}"
                    this.variant = variant
                    this.size = size
                    isEnabled = state != SampleState.Disabled
                    isLoading = state == SampleState.Loading
                    isPressed = state == SampleState.Pressed
                },
                LinearLayout.LayoutParams(wrap),
            )
        }
    }
    addView(TSButtonView(context).apply { text = "Md Focused"; this.variant = variant; focusWhenAttached() }, LinearLayout.LayoutParams(wrap))
    addView(TSButtonView(context).apply { text = "Leading"; this.variant = variant; leadingIcon = TSIcon.ArrowLeft }, LinearLayout.LayoutParams(wrap))
    addView(TSButtonView(context).apply { text = "Trailing"; this.variant = variant; trailingIcon = TSIcon.ArrowRight }, LinearLayout.LayoutParams(wrap))
    addView(
        TSButtonView(context).apply { text = "Block"; this.variant = variant; leadingIcon = TSIcon.Plus },
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
    )
}

internal fun iconButtonGallerySample(context: Context): View = context.galleryColumn().apply {
    TSIconButtonVariant.entries.forEach { variant ->
        listOf(false, true).forEach { round ->
            addView(
                context.galleryRow().apply {
                    listOf(SampleState.Default, SampleState.Pressed, SampleState.Disabled).forEach { state ->
                        addView(
                            TSIconButtonView(context).apply {
                                icon = if (round) TSIcon.Plus else TSIcon.ChevronRight
                                label = state.name
                                this.variant = variant
                                isRound = round
                                isEnabled = state != SampleState.Disabled
                                isPressed = state == SampleState.Pressed
                            },
                        )
                    }
                },
            )
        }
    }
    addView(
        TSIconButtonView(context).apply {
            icon = TSIcon.Plus
            label = "Focused"
            variant = TSIconButtonVariant.Primary
            isRound = true
            focusWhenAttached()
        },
    )
}

internal fun iconGallerySample(context: Context): View = context.galleryColumn().apply {
    TSIconSize.entries.forEach { size ->
        addView(
            context.galleryRow().apply {
                listOf(TSIcon.Home, TSIcon.ChevronLeft, TSIcon.ArrowRight, TSIcon.Logout, TSIcon.Search).forEach { icon ->
                    addView(icon(context, icon, size, TSColorRole.TextPrimary))
                }
            },
        )
    }
    addView(
        context.galleryRow().apply {
            listOf(
                TSIcon.CheckCircle to TSColorRole.Success,
                TSIcon.Alert to TSColorRole.Warning,
                TSIcon.XCircle to TSColorRole.Danger,
                TSIcon.Info to TSColorRole.Info,
            ).forEach { (icon, role) -> addView(icon(context, icon, TSIconSize.Lg, role)) }
        },
    )
}

private fun icon(context: Context, icon: TSIcon, size: TSIconSize, role: TSColorRole): View = AppCompatImageView(context).apply {
    setImageResource(icon.drawableRes)
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(context.tsColor(role)))
    layoutParams = LinearLayout.LayoutParams(context.dp(size.dp), context.dp(size.dp))
}
