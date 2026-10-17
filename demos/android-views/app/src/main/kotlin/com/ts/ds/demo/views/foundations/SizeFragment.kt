package com.ts.ds.demo.views.foundations

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.demo.views.shell.addTokenRow
import com.ts.ds.demo.views.shell.dpToPx
import com.ts.ds.demo.views.shell.formatPx
import com.ts.ds.tokens.R as TokensR
import com.ts.ds.views.R as ViewsR
import com.ts.ds.views.theme.tsColor

private const val PREVIEW_LIMIT_DP = 64f

private val sizeTokens: List<Pair<String, Int>> = listOf(
    "icon-sm" to TokensR.dimen.ts_icon_sm,
    "icon-md" to TokensR.dimen.ts_icon_md,
    "icon-lg" to TokensR.dimen.ts_icon_lg,
    "control-sm" to TokensR.dimen.ts_control_sm,
    "control-md" to TokensR.dimen.ts_control_md,
    "control-lg" to TokensR.dimen.ts_control_lg,
    "nav-bar" to TokensR.dimen.ts_nav_bar,
    "tab-bar" to TokensR.dimen.ts_tab_bar,
    "content-max" to TokensR.dimen.ts_content_max,
)

class SizeFragment : DemoPageFragment(R.layout.page_size) {
    override val page: DemoPageId = DemoPageId.Size

    override fun bindRows(rows: LinearLayout) {
        sizeTokens.forEach { (name, dimen) ->
            val value = resources.getDimension(dimen)
            rows.addTokenRow(name = name, value = requireContext().formatPx(value)) { host ->
                val limit = host.context.dpToPx(PREVIEW_LIMIT_DP)
                host.layoutParams = host.layoutParams.apply {
                    width = limit
                    height = limit
                }
                val size = value.toInt().coerceAtMost(limit)
                host.addView(
                    View(host.context).apply {
                        background = MaterialShapeDrawable(
                            ShapeAppearanceModel.builder(context, 0, ViewsR.style.ShapeAppearance_TS_Xs).build(),
                        ).apply {
                            fillColor = ColorStateList.valueOf(tsColor(ViewsR.attr.tsColorAccentSubtle))
                            setStroke(context.dpToPx(1f).toFloat(), tsColor(ViewsR.attr.tsColorAccentPrimary))
                        }
                    },
                    FrameLayout.LayoutParams(size, size, Gravity.TOP or Gravity.START),
                )
            }
        }
    }
}
