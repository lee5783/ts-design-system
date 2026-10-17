package com.ts.ds.demo.views.foundations

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.demo.views.shell.addTokenRow
import com.ts.ds.demo.views.shell.dpToPx
import com.ts.ds.tokens.R as TokensR
import com.ts.ds.views.R as ViewsR
import com.ts.ds.views.theme.tsColor

class ColorsFragment : DemoPageFragment(R.layout.page_colors) {
    override val page: DemoPageId = DemoPageId.Colors

    override fun bindRows(rows: LinearLayout) {
        TSColorRole.entries.forEach { role ->
            val color = rows.tsColor(role.attr)
            rows.addTokenRow(name = role.name, value = "#%08X".format(color)) { host -> host.addSwatch(color) }
        }
    }

    private fun FrameLayout.addSwatch(color: Int) {
        val size = context.dpToPx(40f)
        addView(
            View(context).apply {
                background = GradientDrawable().apply {
                    setColor(color)
                    cornerRadius = resources.getDimension(TokensR.dimen.ts_radius_sm)
                    setStroke(context.dpToPx(1f), tsColor(ViewsR.attr.tsColorBorderSubtle))
                }
            },
            FrameLayout.LayoutParams(size, size),
        )
    }
}

private val TSColorRole.attr: Int
    get() = when (this) {
        TSColorRole.AccentHover -> ViewsR.attr.tsColorAccentHover
        TSColorRole.AccentPressed -> ViewsR.attr.tsColorAccentPressed
        TSColorRole.AccentPrimary -> ViewsR.attr.tsColorAccentPrimary
        TSColorRole.AccentSubtle -> ViewsR.attr.tsColorAccentSubtle
        TSColorRole.AccentText -> ViewsR.attr.tsColorAccentText
        TSColorRole.BackgroundPrimary -> ViewsR.attr.tsColorBackgroundPrimary
        TSColorRole.BackgroundSecondary -> ViewsR.attr.tsColorBackgroundSecondary
        TSColorRole.BorderStrong -> ViewsR.attr.tsColorBorderStrong
        TSColorRole.BorderSubtle -> ViewsR.attr.tsColorBorderSubtle
        TSColorRole.Danger -> ViewsR.attr.tsColorDanger
        TSColorRole.DangerOnInverse -> ViewsR.attr.tsColorDangerOnInverse
        TSColorRole.DangerPressed -> ViewsR.attr.tsColorDangerPressed
        TSColorRole.DangerSolid -> ViewsR.attr.tsColorDangerSolid
        TSColorRole.DangerSubtle -> ViewsR.attr.tsColorDangerSubtle
        TSColorRole.Focus -> ViewsR.attr.tsColorFocus
        TSColorRole.Info -> ViewsR.attr.tsColorInfo
        TSColorRole.InfoOnInverse -> ViewsR.attr.tsColorInfoOnInverse
        TSColorRole.InfoSubtle -> ViewsR.attr.tsColorInfoSubtle
        TSColorRole.Overlay -> ViewsR.attr.tsColorOverlay
        TSColorRole.Success -> ViewsR.attr.tsColorSuccess
        TSColorRole.SuccessOnInverse -> ViewsR.attr.tsColorSuccessOnInverse
        TSColorRole.SuccessSubtle -> ViewsR.attr.tsColorSuccessSubtle
        TSColorRole.SurfaceCard -> ViewsR.attr.tsColorSurfaceCard
        TSColorRole.SurfaceElevated -> ViewsR.attr.tsColorSurfaceElevated
        TSColorRole.SurfaceHover -> ViewsR.attr.tsColorSurfaceHover
        TSColorRole.SurfaceInverse -> ViewsR.attr.tsColorSurfaceInverse
        TSColorRole.SurfacePressed -> ViewsR.attr.tsColorSurfacePressed
        TSColorRole.SwitchThumb -> ViewsR.attr.tsColorSwitchThumb
        TSColorRole.TextDisabled -> ViewsR.attr.tsColorTextDisabled
        TSColorRole.TextInverse -> ViewsR.attr.tsColorTextInverse
        TSColorRole.TextMuted -> ViewsR.attr.tsColorTextMuted
        TSColorRole.TextOnAccent -> ViewsR.attr.tsColorTextOnAccent
        TSColorRole.TextPrimary -> ViewsR.attr.tsColorTextPrimary
        TSColorRole.TextSecondary -> ViewsR.attr.tsColorTextSecondary
        TSColorRole.Warning -> ViewsR.attr.tsColorWarning
        TSColorRole.WarningOnInverse -> ViewsR.attr.tsColorWarningOnInverse
        TSColorRole.WarningSubtle -> ViewsR.attr.tsColorWarningSubtle
    }
