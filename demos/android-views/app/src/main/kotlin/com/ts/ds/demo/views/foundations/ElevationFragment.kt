package com.ts.ds.demo.views.foundations

import android.content.res.ColorStateList
import android.content.res.Configuration
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
import com.ts.ds.tokens.TSShadowValues
import com.ts.ds.tokens.TSTokenValues
import com.ts.ds.views.R as ViewsR
import com.ts.ds.views.theme.tsColor

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
