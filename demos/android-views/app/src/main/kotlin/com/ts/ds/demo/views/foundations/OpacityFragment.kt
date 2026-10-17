package com.ts.ds.demo.views.foundations

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.demo.views.shell.addTokenRow
import com.ts.ds.demo.views.shell.dpToPx
import com.ts.ds.tokens.R as TokensR
import com.ts.ds.views.R as ViewsR
import com.ts.ds.views.theme.tsColor

class OpacityFragment : DemoPageFragment(R.layout.page_opacity) {
    override val page: DemoPageId = DemoPageId.Opacity

    override fun bindRows(rows: LinearLayout) {
        listOf(
            "opacity-full" to 1f,
            "opacity-pressed" to ResourcesCompat.getFloat(resources, TokensR.dimen.ts_opacity_pressed),
            "opacity-disabled" to ResourcesCompat.getFloat(resources, TokensR.dimen.ts_opacity_disabled),
        ).forEach { (name, value) ->
            rows.addTokenRow(name = name, value = "${Math.round(value * 100)}%") { host ->
                val size = host.context.dpToPx(40f)
                host.addView(
                    View(host.context).apply {
                        alpha = value
                        background = GradientDrawable().apply {
                            setColor(tsColor(ViewsR.attr.tsColorAccentPrimary))
                            cornerRadius = resources.getDimension(TokensR.dimen.ts_radius_sm)
                        }
                    },
                    FrameLayout.LayoutParams(size, size),
                )
            }
        }
    }
}
