package com.ts.ds.demo.views.foundations

import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.demo.views.shell.addTokenRow
import com.ts.ds.demo.views.shell.formatPx
import com.ts.ds.tokens.R as TokensR
import com.ts.ds.views.R as ViewsR
import com.ts.ds.views.theme.tsColor

private val spacingTokens: List<Pair<String, Int>> = listOf(
    "space-0.5" to TokensR.dimen.ts_space_0_5,
    "space-1" to TokensR.dimen.ts_space_1,
    "space-2" to TokensR.dimen.ts_space_2,
    "space-3" to TokensR.dimen.ts_space_3,
    "space-4" to TokensR.dimen.ts_space_4,
    "space-5" to TokensR.dimen.ts_space_5,
    "space-6" to TokensR.dimen.ts_space_6,
    "space-8" to TokensR.dimen.ts_space_8,
    "space-10" to TokensR.dimen.ts_space_10,
    "space-12" to TokensR.dimen.ts_space_12,
    "space-16" to TokensR.dimen.ts_space_16,
)

class SpacingFragment : DemoPageFragment(R.layout.page_spacing) {
    override val page: DemoPageId = DemoPageId.Spacing

    override fun bindRows(rows: LinearLayout) {
        spacingTokens.forEach { (name, dimen) ->
            val value = resources.getDimension(dimen)
            rows.addTokenRow(name = name, value = requireContext().formatPx(value)) { host ->
                host.addView(
                    View(host.context).apply { setBackgroundColor(tsColor(ViewsR.attr.tsColorAccentPrimary)) },
                    FrameLayout.LayoutParams(value.toInt(), resources.getDimensionPixelSize(TokensR.dimen.ts_icon_md)),
                )
            }
        }
    }
}
