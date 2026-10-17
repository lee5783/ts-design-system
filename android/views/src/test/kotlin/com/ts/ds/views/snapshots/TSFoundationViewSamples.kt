package com.ts.ds.views.snapshots

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.textview.MaterialTextView
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.views.R
import com.ts.ds.views.theme.tsColor

internal const val SAMPLE_TEXT = "The quick brown fox"

private fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

private fun styleFor(role: TSTypeRole): Int = R.style::class.java.getField("TextAppearance_TS_${role.name}").getInt(null)

private fun attrFor(role: TSColorRole): Int = R.attr::class.java.getField("tsColor${role.name}").getInt(null)

private fun Context.sampleColumn(): LinearLayout = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    setBackgroundColor(tsColor(R.attr.tsColorBackgroundPrimary))
    val padding = dp(16)
    setPadding(padding, padding, padding, padding)
    showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
    dividerDrawable = GradientDrawable().apply { setSize(0, dp(8)) }
}

internal fun typographySample(context: Context): View = context.sampleColumn().apply {
    TSTypeRole.entries.forEach { role ->
        addView(
            MaterialTextView(context).apply {
                setTextAppearance(styleFor(role))
                setTextColor(context.tsColor(R.attr.tsColorTextPrimary))
                text = "${role.name} $SAMPLE_TEXT"
            },
        )
    }
}

internal fun colorSwatchesSample(context: Context): View = context.sampleColumn().apply {
    TSColorRole.entries.chunked(2).forEach { pair ->
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            dividerDrawable = GradientDrawable().apply { setSize(context.dp(8), 0) }
        }
        pair.forEach { role -> row.addView(swatch(context, role), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)) }
        if (pair.size == 1) row.addView(View(context), LinearLayout.LayoutParams(0, 0, 1f))
        addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
    }
}

private fun swatch(context: Context, role: TSColorRole): View = LinearLayout(context).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = Gravity.CENTER_VERTICAL
    val size = context.dp(24)
    addView(
        View(context).apply {
            background = GradientDrawable().apply {
                setColor(context.tsColor(attrFor(role)))
                setStroke(context.dp(1), context.tsColor(R.attr.tsColorBorderStrong))
            }
        },
        LinearLayout.LayoutParams(size, size).apply { marginEnd = context.dp(8) },
    )
    addView(
        MaterialTextView(context).apply {
            setTextAppearance(R.style.TextAppearance_TS_Caption)
            setTextColor(context.tsColor(R.attr.tsColorTextSecondary))
            text = role.name
        },
    )
}
