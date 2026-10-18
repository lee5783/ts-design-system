package com.tinysol.designsystem.views.sample.foundations

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.views.sample.R
import com.tinysol.designsystem.views.sample.shell.DemoPageFragment
import com.tinysol.designsystem.views.sample.shell.addTokenRow
import com.tinysol.designsystem.views.sample.shell.dpToPx
import com.tinysol.designsystem.tokens.R as TokensR
import com.tinysol.designsystem.views.R as ViewsR
import com.tinysol.designsystem.views.theme.tsColor

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
