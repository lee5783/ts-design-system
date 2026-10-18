package com.tinysol.designsystem.views.sample.components

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.tinysol.designsystem.core.style.TSSpinnerSize
import com.tinysol.designsystem.core.style.TSSpinnerTone
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.views.sample.R
import com.tinysol.designsystem.views.sample.shell.DemoPageFragment
import com.tinysol.designsystem.tokens.R as TokensR
import com.tinysol.designsystem.views.components.spinner.TSSpinnerView

class SpinnerFragment : DemoPageFragment(R.layout.page_spinner) {
    override val page: DemoPageId = DemoPageId.Spinner

    override fun bindRows(rows: LinearLayout) {
        bindSizes(rows.section())
        bindTones(rows.section())
        bindInContext(rows.section())
    }

    private fun LinearLayout.section(): LinearLayout = LinearLayout(context).also {
        it.orientation = LinearLayout.VERTICAL
        addView(it, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
    }

    private fun spinner(size: TSSpinnerSize = TSSpinnerSize.Md, tone: TSSpinnerTone = TSSpinnerTone.Primary): TSSpinnerView =
        TSSpinnerView(requireContext()).apply {
            this.size = size
            this.tone = tone
        }

    private fun bindSizes(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_sizes)
        val row = section.addFlowRow()
        TSSpinnerSize.entries.forEach { size -> row.addView(spinner(size = size).apply { tag = size(size) }) }
    }

    private fun bindTones(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_tones)
        TSSpinnerTone.entries.forEach { tone ->
            val row = section.addFlowRow()
            row.addView(spinner(tone = tone).apply { tag = tone(tone) })
            row.addCaption(tone.name.lowercase())
        }
    }

    private fun bindInContext(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_in_context)
        val loading = getString(DemoR.string.demo_sample_loading_messages)
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            tag = IN_CONTEXT
            isFocusable = true
        }
        row.addView(
            spinner(TSSpinnerSize.Sm, TSSpinnerTone.Secondary),
        )
        row.addCaption(loading).apply { setPaddingRelative(resources.getDimensionPixelSize(TokensR.dimen.ts_space_2), 0, 0, 0) }
        section.addSpaced(row, matchWidth = true)
        section.addSpaced(
            spinner(TSSpinnerSize.Lg, TSSpinnerTone.Accent).apply {
                tag = LABELLED
            },
        )
    }

    companion object {
        const val IN_CONTEXT = "demo.spinner.in-context"
        const val LABELLED = "demo.spinner.labelled"

        fun size(size: TSSpinnerSize): String = "demo.spinner.size.${size.name.lowercase()}"

        fun tone(tone: TSSpinnerTone): String = "demo.spinner.tone.${tone.name.lowercase()}"
    }
}
