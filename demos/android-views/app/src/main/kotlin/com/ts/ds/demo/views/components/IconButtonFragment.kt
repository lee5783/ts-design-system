package com.ts.ds.demo.views.components

import android.widget.LinearLayout
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSIconButtonVariant
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.views.components.iconbutton.TSIconButtonView

class IconButtonFragment : DemoPageFragment(R.layout.page_icon_button) {
    override val page: DemoPageId = DemoPageId.IconButton

    private var playgroundVariant = TSIconButtonVariant.Ghost

    override fun bindRows(rows: LinearLayout) {
        listOf(R.id.icon_button_consumer_icon, R.id.icon_button_consumer_icon_secondary, R.id.icon_button_consumer_icon_round).forEach {
            rows.findViewById<TSIconButtonView>(it).setIconResource(R.drawable.demo_ic_controller)
        }
        bindVariants(rows.findViewById(R.id.icon_button_variants))
        bindStates(rows.findViewById(R.id.icon_button_states))
        bindPlayground(rows.findViewById(R.id.icon_button_playground))
    }

    private fun bindVariants(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_variants)
        TSIconButtonVariant.entries.forEach { variant ->
            val row = section.addFlowRow()
            row.addView(iconButton(section, TSIcon.Search, DemoR.string.demo_sample_search, variant))
            row.addCaption(variant.name.lowercase())
        }
    }

    private fun bindStates(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_states)
        TSIconButtonVariant.entries.forEach { variant ->
            listOf(false, true).forEach { round ->
                val name = variant.name.lowercase()
                section.addCaption(if (round) "$name · ${getString(DemoR.string.demo_component_round)}" else name)
                val row = section.addFlowRow()
                listOf(true, false).forEach { enabled ->
                    row.addView(
                        iconButton(section, TSIcon.Close, DemoR.string.demo_sample_close, variant).apply {
                            isRound = round
                            isEnabled = enabled
                        },
                    )
                }
            }
        }
    }

    private fun bindPlayground(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_playground)
        val button = iconButton(section, TSIcon.Settings, DemoR.string.demo_sample_more_options, playgroundVariant).apply {
            tag = PLAYGROUND_BUTTON
        }
        section.addCaption(getString(DemoR.string.demo_component_variant))
        section.addChoiceGroup(TSIconButtonVariant.entries, { it.name.lowercase() }, playgroundVariant) {
            playgroundVariant = it
            button.variant = it
        }
        section.addToggle(DemoR.string.demo_component_round) { button.isRound = it }
        section.addToggle(DemoR.string.demo_component_disabled, tag = PLAYGROUND_DISABLED) { button.isEnabled = !it }
        section.addSpaced(button)
        val counter = section.addTapCounter(PLAYGROUND_COUNT)
        button.setOnClickListener { counter.increment() }
    }

    private fun iconButton(section: LinearLayout, icon: TSIcon, label: Int, variant: TSIconButtonVariant) =
        TSIconButtonView(section.context).apply {
            this.icon = icon
            this.label = getString(label)
            this.variant = variant
        }

    companion object {
        const val PLAYGROUND_BUTTON = "demo.icon-button.playground.button"
        const val PLAYGROUND_COUNT = "demo.icon-button.playground.count"
        const val PLAYGROUND_DISABLED = "demo.icon-button.playground.disabled"
    }
}
