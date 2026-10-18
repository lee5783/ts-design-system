package com.ts.ds.views.components.badge

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import com.google.android.material.textview.MaterialTextView
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.icon.TSIcons
import com.ts.ds.core.style.TSBadgeStyleSpec
import com.ts.ds.core.style.TSBadgeTone
import com.ts.ds.core.style.resolve
import com.ts.ds.views.R
import com.ts.ds.views.components.shared.dpToPx
import com.ts.ds.views.theme.textAppearance
import com.ts.ds.views.theme.tsColor

public class TSBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    private val dotView = View(context)
    private val iconView = AppCompatImageView(context)
    private val labelView = MaterialTextView(context)
    private val chrome = GradientDrawable()
    private val dotShape = GradientDrawable().apply { shape = GradientDrawable.OVAL }
    private var ready = false

    public var text: CharSequence
        get() = labelView.text
        set(value) {
            labelView.text = value
        }

    public var tone: TSBadgeTone = TSBadgeTone.Neutral
        set(value) {
            field = value
            applyConfiguration()
        }

    public var showsDot: Boolean = false
        set(value) {
            field = value
            applyConfiguration()
        }

    public var icon: TSIcon? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = chrome
        dotView.background = dotShape
        listOf(dotView, iconView).forEach { it.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }
        val initial = TSBadgeStyleSpec.resolve(TSBadgeStyleSpec.Configuration())
        val dotSize = dpToPx(initial.dotSizeDp)
        val iconSize = dpToPx(initial.iconSizeDp)
        val spacing = dpToPx(initial.spacingDp)
        addView(dotView, LayoutParams(dotSize, dotSize).apply { marginEnd = spacing })
        addView(iconView, LayoutParams(iconSize, iconSize).apply { marginEnd = spacing })
        addView(labelView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        labelView.maxLines = initial.maxLines
        labelView.isSingleLine = true
        labelView.ellipsize = TextUtils.TruncateAt.END
        labelView.textAlignment = TEXT_ALIGNMENT_VIEW_START
        context.obtainStyledAttributes(attrs, R.styleable.TSBadgeView, defStyleAttr, 0).apply {
            text = getText(R.styleable.TSBadgeView_android_text) ?: ""
            tone = TSBadgeTone.entries[getInt(R.styleable.TSBadgeView_tsBadgeTone, TSBadgeTone.Neutral.ordinal)]
            showsDot = getBoolean(R.styleable.TSBadgeView_tsShowsDot, false)
            icon = getString(R.styleable.TSBadgeView_tsIcon)?.let(TSIcons::fromDsName)
            recycle()
        }
        ready = true
        applyConfiguration()
    }

    internal val resolved: TSBadgeStyleSpec.Resolved
        get() = TSBadgeStyleSpec.resolve(TSBadgeStyleSpec.Configuration(tone, showsDot, icon != null))

    internal val label: TextView
        get() = labelView

    private fun applyConfiguration() {
        if (!ready) return
        val resolved = resolved
        val foreground = tsColor(resolved.foreground)
        labelView.setTextAppearance(resolved.typeRole.textAppearance)
        labelView.typeface = Typeface.create(MEDIUM_FAMILY, Typeface.NORMAL)
        labelView.setTextColor(foreground)
        val horizontal = dpToPx(resolved.horizontalPaddingDp)
        val vertical = dpToPx(resolved.verticalPaddingDp)
        setPaddingRelative(horizontal, vertical, horizontal, vertical)
        minimumHeight = dpToPx(resolved.minHeightDp)
        chrome.cornerRadius = dpToPx(resolved.cornerRadiusDp).toFloat()
        chrome.setColor(tsColor(resolved.fill))
        chrome.setStroke(dpToPx(resolved.borderWidthDp), resolved.border?.let(::tsColor) ?: 0)
        dotShape.setColor(foreground)
        dotView.visibility = if (resolved.showsDot) VISIBLE else GONE
        val currentIcon = icon
        if (currentIcon != null && resolved.showsIcon) {
            iconView.setImageResource(currentIcon.drawableRes)
            iconView.visibility = VISIBLE
        } else {
            iconView.setImageDrawable(null)
            iconView.visibility = GONE
        }
        ImageViewCompat.setImageTintList(iconView, ColorStateList.valueOf(foreground))
    }

    private companion object {
        const val MEDIUM_FAMILY = "sans-serif-medium"
    }
}
