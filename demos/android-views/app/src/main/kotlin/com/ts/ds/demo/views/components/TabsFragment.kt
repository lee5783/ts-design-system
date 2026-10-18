package com.ts.ds.demo.views.components

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.ts.ds.core.style.TSTabItem
import com.ts.ds.core.style.TSTabsVariant
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.sample.SampleData
import com.ts.ds.demo.support.sample.SampleTab
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.views.components.tabs.TSTabsView

class TabsFragment : DemoPageFragment(R.layout.page_tabs) {
    override val page: DemoPageId = DemoPageId.Tabs

    private var playgroundVariant = TSTabsVariant.Segmented

    override fun bindRows(rows: LinearLayout) {
        rows.markSectionHeadings()
        bindStates(rows.findViewById(R.id.tabs_states))
        bindOverflow(rows.findViewById(R.id.tabs_overflow))
        bindPlayground(rows.findViewById(R.id.tabs_playground))
    }

    private fun List<SampleTab>.tabItems(context: Context): List<TSTabItem> = map { TSTabItem(it.id, context.getString(it.labelRes)) }

    private fun variantName(variant: TSTabsVariant): String = getString(
        when (variant) {
            TSTabsVariant.Underline -> DemoR.string.demo_component_underline
            TSTabsVariant.Segmented -> DemoR.string.demo_component_segmented
        },
    )

    private fun selectedCaption(items: List<TSTabItem>, id: String): String =
        getString(DemoR.string.demo_component_selected_value, items.firstOrNull { it.id == id }?.label ?: id)

    private fun ViewGroup.addTabs(tabs: TSTabsView): TSTabsView = addSpaced(tabs, matchWidth = true) as TSTabsView

    private fun bindStates(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_states)
        TSTabsVariant.entries.forEach { variant ->
            section.addCaption(getString(DemoR.string.demo_component_value_disabled, variantName(variant)))
            section.addTabs(
                TSTabsView(section.context).apply {
                    items = SampleData.tabRanges.take(3).tabItems(context)
                    selectedId = SampleData.tabRanges[1].id
                    this.variant = variant
                    isBlock = true
                    isEnabled = false
                },
            )
        }
    }

    private fun bindOverflow(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_overflow)
        val items = SampleData.tabManySections.tabItems(section.context)
        val tabs = section.addTabs(
            TSTabsView(section.context).apply {
                this.items = items
                selectedId = items.first().id
                tag = OVERFLOW
            },
        )
        val value = section.addCaption(selectedCaption(items, tabs.selectedId)).apply { tag = OVERFLOW_VALUE }
        tabs.setOnTabSelectedListener { _, id -> value.text = selectedCaption(items, id) }
    }

    private fun bindPlayground(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_playground)
        val items = SampleData.tabRanges.tabItems(section.context)
        val tabs = TSTabsView(section.context).apply {
            this.items = items
            selectedId = items.first().id
            variant = playgroundVariant
            isBlock = true
            tag = PLAYGROUND_TABS
        }
        section.addCaption(getString(DemoR.string.demo_component_variant))
        section.addChoiceGroup(TSTabsVariant.entries, { it.name.lowercase() }, playgroundVariant, { "demo.tabs.playground.variant.${it.name.lowercase()}" }) {
            playgroundVariant = it
            tabs.variant = it
        }
        section.addToggle(DemoR.string.demo_component_disabled, tag = PLAYGROUND_DISABLED) { tabs.isEnabled = !it }
        section.addToggle(DemoR.string.demo_component_block, checked = true, tag = PLAYGROUND_BLOCK) { tabs.isBlock = it }
        section.addTabs(tabs)
        val value: TextView = section.addCaption(selectedCaption(items, tabs.selectedId)).apply { tag = PLAYGROUND_VALUE }
        val counter = section.addTapCounter(PLAYGROUND_COUNT)
        tabs.setOnTabSelectedListener { _, id ->
            value.text = selectedCaption(items, id)
            counter.increment()
        }
    }

    companion object {
        const val OVERFLOW = "demo.tabs.overflow"
        const val OVERFLOW_VALUE = "demo.tabs.overflow.value"
        const val PLAYGROUND_TABS = "demo.tabs.playground.tabs"
        const val PLAYGROUND_DISABLED = "demo.tabs.playground.disabled"
        const val PLAYGROUND_BLOCK = "demo.tabs.playground.block"
        const val PLAYGROUND_VALUE = "demo.tabs.playground.value"
        const val PLAYGROUND_COUNT = "demo.tabs.playground.count"
    }
}
