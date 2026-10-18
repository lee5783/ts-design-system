package com.tinysol.designsystem.views.sample.foundations

import android.content.res.ColorStateList
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.views.sample.R
import com.tinysol.designsystem.views.sample.shell.DemoPageFragment
import com.tinysol.designsystem.views.sample.shell.addTokenRow
import com.tinysol.designsystem.views.sample.shell.dpToPx
import com.tinysol.designsystem.views.sample.shell.formatPx
import com.tinysol.designsystem.tokens.R as TokensR
import com.tinysol.designsystem.views.R as ViewsR
import com.tinysol.designsystem.views.theme.tsColor

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
