package com.ts.ds.demo.views.components

import android.view.ViewGroup
import android.widget.LinearLayout
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSButtonSize
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.tokens.R as TokensR
import com.ts.ds.views.components.button.TSButtonView

class ButtonFragment : DemoPageFragment(R.layout.page_button) {
    override val page: DemoPageId = DemoPageId.Button

    private var playgroundVariant = TSButtonVariant.Primary
    private var playgroundSize = TSButtonSize.Md

    override fun bindRows(rows: LinearLayout) {
        listOf(R.id.button_consumer_icon, R.id.button_consumer_icon_secondary, R.id.button_consumer_icon_loading, R.id.button_line_limit).forEach {
            rows.findViewById<TSButtonView>(it).setLeadingIconResource(R.drawable.demo_ic_controller)
        }
        bindStates(rows.findViewById(R.id.button_states))
        bindPlayground(rows.findViewById(R.id.button_playground))
    }

    private fun bindStates(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_states)
        TSButtonVariant.entries.forEach { variant ->
            TSButtonSize.entries.forEach { size ->
                section.addCaption("${variant.name.lowercase()} · ${size.name.lowercase()}")
                val row = section.addFlowRow()
                listOf(
                    DemoR.string.demo_component_default to Pair(true, false),
                    DemoR.string.demo_component_disabled to Pair(false, false),
                    DemoR.string.demo_component_loading to Pair(true, true),
                ).forEach { (label, flags) ->
                    row.addView(
                        TSButtonView(section.context).apply {
                            text = getString(label)
                            this.variant = variant
                            this.size = size
                            isEnabled = flags.first
                            isLoading = flags.second
                        },
                    )
                }
                (row.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = resources.getDimensionPixelSize(TokensR.dimen.ts_space_3)
            }
        }
    }

    private fun bindPlayground(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_playground)
        val button = TSButtonView(section.context).apply {
            text = getString(DemoR.string.demo_sample_save)
            tag = PLAYGROUND_BUTTON
        }
        section.addCaption(getString(DemoR.string.demo_component_variant))
        section.addChoiceGroup(TSButtonVariant.entries, { it.name.lowercase() }, playgroundVariant, { "demo.button.playground.variant.${it.name.lowercase()}" }) {
            playgroundVariant = it
            button.variant = it
        }
        section.addCaption(getString(DemoR.string.demo_component_size))
        section.addChoiceGroup(TSButtonSize.entries, { it.name.lowercase() }, playgroundSize) {
            playgroundSize = it
            button.size = it
        }
        section.addToggle(DemoR.string.demo_component_loading, tag = PLAYGROUND_LOADING) { button.isLoading = it }
        section.addToggle(DemoR.string.demo_component_disabled, tag = PLAYGROUND_DISABLED) { button.isEnabled = !it }
        section.addToggle(DemoR.string.demo_component_block) {
            button.layoutParams = button.layoutParams.apply {
                width = if (it) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
        section.addToggle(DemoR.string.demo_component_leading_icon) { button.leadingIcon = if (it) TSIcon.Check else null }
        section.addToggle(DemoR.string.demo_component_trailing_icon) { button.trailingIcon = if (it) TSIcon.ArrowRight else null }
        section.addSpaced(button)
        val counter = section.addTapCounter(PLAYGROUND_COUNT)
        button.setOnClickListener { counter.increment() }
    }

    companion object {
        const val PLAYGROUND_BUTTON = "demo.button.playground.button"
        const val PLAYGROUND_COUNT = "demo.button.playground.count"
        const val PLAYGROUND_LOADING = "demo.button.playground.loading"
        const val PLAYGROUND_DISABLED = "demo.button.playground.disabled"
    }
}
