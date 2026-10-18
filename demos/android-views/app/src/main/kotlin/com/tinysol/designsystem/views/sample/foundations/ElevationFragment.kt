package com.tinysol.designsystem.views.sample.foundations

import android.content.res.ColorStateList
import android.content.res.Configuration
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
import com.tinysol.designsystem.tokens.TSShadowValues
import com.tinysol.designsystem.tokens.TSTokenValues
import com.tinysol.designsystem.views.R as ViewsR
import com.tinysol.designsystem.views.theme.tsColor

private val shadowTokens: List<Pair<String, TSShadowValues>> = listOf(
    "shadow-xs" to TSTokenValues.Shadow.shadowXs,
    "shadow-sm" to TSTokenValues.Shadow.shadowSm,
    "shadow-md" to TSTokenValues.Shadow.shadowMd,
    "shadow-lg" to TSTokenValues.Shadow.shadowLg,
)

class ElevationFragment : DemoPageFragment(R.layout.page_elevation) {
    override val page: DemoPageId = DemoPageId.Elevation

    override fun bindRows(rows: LinearLayout) {
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        shadowTokens.forEach { (name, shadow) ->
            val elevation = shadow.elevation
            val value = if (elevation % 1f == 0f) "${elevation.toInt()}dp" else "${elevation}dp"
            rows.addTokenRow(name = name, value = value) { host ->
                val inset = host.context.dpToPx(12f)
                host.setPadding(inset, inset, inset, inset)
                val size = host.context.dpToPx(48f)
                host.addView(
                    View(host.context).apply {
                        background = MaterialShapeDrawable(
                            ShapeAppearanceModel.builder(context, 0, ViewsR.style.ShapeAppearance_TS_Md).build(),
                        ).apply {
                            fillColor = ColorStateList.valueOf(tsColor(ViewsR.attr.tsColorSurfaceElevated))
                            shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
                            setShadowColor((if (dark) shadow.darkColor else shadow.lightColor).toInt())
                            this.elevation = context.dpToPx(elevation).toFloat()
                        }
                    },
                    FrameLayout.LayoutParams(size, size),
                )
            }
        }
    }
}
