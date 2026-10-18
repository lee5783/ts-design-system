package com.ts.ds.views.snapshots

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
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

internal class SampleControllerDrawable(private val sizePx: Int) : Drawable() {
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeCap = Paint.Cap.ROUND
    }
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun getIntrinsicWidth(): Int = sizePx

    override fun getIntrinsicHeight(): Int = sizePx

    override fun draw(canvas: Canvas) {
        val unit = bounds.width() / 24f
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        stroke.strokeWidth = 1.75f * unit
        canvas.drawRoundRect(RectF(2 * unit, 7 * unit, 22 * unit, 17 * unit), 5 * unit, 5 * unit, stroke)
        canvas.drawLine(7 * unit, 10 * unit, 7 * unit, 14 * unit, stroke)
        canvas.drawLine(5 * unit, 12 * unit, 9 * unit, 12 * unit, stroke)
        dot.color = Color.RED
        canvas.drawCircle(16 * unit, 11 * unit, unit, dot)
        dot.color = Color.BLUE
        canvas.drawCircle(18 * unit, 13 * unit, unit, dot)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        stroke.alpha = alpha
        dot.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        stroke.colorFilter = colorFilter
        dot.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

internal fun Context.sampleController(sizeDp: Float = 24f): Drawable = SampleControllerDrawable(dp(sizeDp))

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

internal fun buttonCustomIconSample(context: Context, variant: TSButtonVariant): View = context.galleryColumn().apply {
    TSButtonSize.entries.forEach { size ->
        addView(
            context.galleryRow().apply {
                addView(TSButtonView(context).apply { text = size.name; this.variant = variant; this.size = size; leadingIcon = TSIcon.Plus })
                addView(TSButtonView(context).apply { text = size.name; this.variant = variant; this.size = size; leadingIconDrawable = context.sampleController() })
            },
        )
    }
    listOf(SampleState.Pressed, SampleState.Disabled, SampleState.Loading).forEach { state ->
        addView(
            TSButtonView(context).apply {
                text = state.name
                this.variant = variant
                leadingIconDrawable = context.sampleController()
                trailingIconDrawable = context.sampleController()
                isEnabled = state != SampleState.Disabled
                isLoading = state == SampleState.Loading
                isPressed = state == SampleState.Pressed
            },
            LinearLayout.LayoutParams(wrap),
        )
    }
    addView(TSButtonView(context).apply { text = "Large drawable"; this.variant = variant; leadingIconDrawable = context.sampleController(48f) }, LinearLayout.LayoutParams(wrap))
    addView(
        TSButtonView(context).apply {
            text = "Join the session with a very long label"
            this.variant = variant
            leadingIconDrawable = context.sampleController()
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        },
        LinearLayout.LayoutParams(context.dp(220f), ViewGroup.LayoutParams.WRAP_CONTENT),
    )
    addView(
        TSButtonView(context).apply {
            text = "Join the session with a very long label"
            this.variant = variant
            leadingIconDrawable = context.sampleController()
        },
        LinearLayout.LayoutParams(context.dp(220f), ViewGroup.LayoutParams.WRAP_CONTENT),
    )
}

internal fun iconButtonCustomIconSample(context: Context): View = context.galleryColumn().apply {
    TSIconButtonVariant.entries.forEach { variant ->
        addView(
            context.galleryRow().apply {
                addView(TSIconButtonView(context).apply { icon = TSIcon.Plus; label = "Add"; this.variant = variant })
                listOf(SampleState.Default, SampleState.Pressed, SampleState.Disabled).forEach { state ->
                    addView(
                        TSIconButtonView(context).apply {
                            iconDrawable = context.sampleController()
                            label = state.name
                            this.variant = variant
                            isEnabled = state != SampleState.Disabled
                            isPressed = state == SampleState.Pressed
                        },
                    )
                }
                addView(TSIconButtonView(context).apply { iconDrawable = context.sampleController(48f); label = "Large"; this.variant = variant; isRound = true })
            },
        )
    }
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
