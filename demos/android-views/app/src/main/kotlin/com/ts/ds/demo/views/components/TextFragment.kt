package com.ts.ds.demo.views.components

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSTextColor
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.tokens.R as TokensR
import com.ts.ds.views.theme.textAppearance
import com.ts.ds.views.theme.tsColor

class TextFragment : DemoPageFragment(R.layout.page_text) {
    override val page: DemoPageId = DemoPageId.Text

    override fun bindRows(rows: LinearLayout) {
        bindRoles(rows.section())
        bindColors(rows.section())
        bindTruncation(rows.section())
    }

    private fun LinearLayout.section(): LinearLayout = LinearLayout(context).also {
        it.orientation = LinearLayout.VERTICAL
        addView(it, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
    }

    private fun text(content: CharSequence, role: TSTypeRole, color: TSTextColor = TSTextColor.Primary): TextView =
        TextView(requireContext()).apply {
            setTextAppearance(role.textAppearance)
            setTextColor(context.tsColor(color.role))
            textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            isAllCaps = role == TSTypeRole.Overline
            text = content
        }

    private fun bindRoles(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_type_roles)
        TSTypeRole.entries.forEach { role ->
            section.addCaption(role.name.lowercase())
            section.addSpaced(text(getString(DemoR.string.demo_sample_pangram), role).apply { tag = role(role) }, matchWidth = true)
        }
    }

    private fun bindColors(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_text_colors)
        val padding = resources.getDimensionPixelSize(TokensR.dimen.ts_space_2)
        TSTextColor.entries.forEach { color ->
            val background = when (color) {
                TSTextColor.Inverse -> requireContext().tsColor(TSColorRole.SurfaceInverse)
                TSTextColor.OnAccent -> requireContext().tsColor(TSColorRole.AccentPrimary)
                else -> Color.TRANSPARENT
            }
            section.addSpaced(
                text(color.name.lowercase(), TSTypeRole.Body, color).apply {
                    tag = color(color)
                    setBackgroundColor(background)
                    setPadding(padding, padding, padding, padding)
                },
                matchWidth = true,
            )
        }
    }

    private fun bindTruncation(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_truncation)
        section.addSpaced(
            text(getString(DemoR.string.demo_sample_long_setting), TSTypeRole.Body).apply {
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                tag = TRUNCATED
            },
            matchWidth = true,
        )
    }

    companion object {
        const val TRUNCATED = "demo.text.truncated"

        fun role(role: TSTypeRole): String = "demo.text.role.${role.name.lowercase()}"

        fun color(color: TSTextColor): String = "demo.text.color.${color.name.lowercase()}"
    }
}
