package com.ts.ds.demo.views.foundations

import android.content.res.ColorStateList
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

private data class RadiusToken(val name: String, val dimen: Int, val shapeAppearance: Int)

private val radiusTokens: List<RadiusToken> = listOf(
    RadiusToken("radius-xs", TokensR.dimen.ts_radius_xs, ViewsR.style.ShapeAppearance_TS_Xs),
    RadiusToken("radius-sm", TokensR.dimen.ts_radius_sm, ViewsR.style.ShapeAppearance_TS_Sm),
    RadiusToken("radius-md", TokensR.dimen.ts_radius_md, ViewsR.style.ShapeAppearance_TS_Md),
    RadiusToken("radius-lg", TokensR.dimen.ts_radius_lg, ViewsR.style.ShapeAppearance_TS_Lg),
    RadiusToken("radius-xl", TokensR.dimen.ts_radius_xl, ViewsR.style.ShapeAppearance_TS_Xl),
    RadiusToken("radius-full", TokensR.dimen.ts_radius_full, ViewsR.style.ShapeAppearance_TS_Full),
)

class RadiusFragment : DemoPageFragment(R.layout.page_radius) {
    override val page: DemoPageId = DemoPageId.Radius

    override fun bindRows(rows: LinearLayout) {
        radiusTokens.forEach { token ->
            val value = requireContext().formatPx(resources.getDimension(token.dimen))
            rows.addTokenRow(name = token.name, value = value) { host ->
                val size = host.context.dpToPx(56f)
                host.addView(
                    View(host.context).apply {
                        background = MaterialShapeDrawable(
                            ShapeAppearanceModel.builder(context, 0, token.shapeAppearance).build(),
                        ).apply {
                            fillColor = ColorStateList.valueOf(tsColor(ViewsR.attr.tsColorAccentSubtle))
                            setStroke(context.dpToPx(2f).toFloat(), tsColor(ViewsR.attr.tsColorAccentPrimary))
                        }
                    },
                    FrameLayout.LayoutParams(size, size),
                )
            }
        }
    }
}
