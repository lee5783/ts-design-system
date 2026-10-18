package com.ts.ds.demo.views.components

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.icon.TSIcons
import com.ts.ds.core.style.TSBottomNavItem
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.sample.SampleData
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.demo.views.shell.openDestination
import com.ts.ds.views.components.bottomnav.TSBottomNavView
import com.ts.ds.views.components.button.TSButtonView

internal fun Context.navItems(count: Int, inboxBadge: Int? = null, alertsBadge: Int? = null): List<TSBottomNavItem> =
    SampleData.navItems(count).map {
        TSBottomNavItem(
            id = it.id,
            label = getString(it.labelRes),
            icon = TSIcons.fromDsName(it.iconName) ?: TSIcon.Info,
            badge = when (it.id) {
                SampleData.NAV_INBOX -> inboxBadge
                SampleData.NAV_ALERTS -> alertsBadge
                else -> null
            },
        )
    }

class BottomNavFragment : DemoPageFragment(R.layout.page_bottom_nav) {
    override val page: DemoPageId = DemoPageId.BottomNav

    private var itemCount = 4
    private var badge by pageState.int(3)

    override fun bindRows(rows: LinearLayout) {
        bindVariants(rows.findViewById(R.id.bottom_nav_variants))
        bindStates(rows.findViewById(R.id.bottom_nav_states))
        bindShell(rows.findViewById(R.id.bottom_nav_shell))
        bindPlayground(rows.findViewById(R.id.bottom_nav_playground))
    }

    private fun selectedCaption(id: String): String {
        val label = SampleData.navItems.firstOrNull { it.id == id }?.labelRes?.let(::getString) ?: id
        return getString(DemoR.string.demo_component_selected_value, label)
    }

    private fun ViewGroup.addNav(items: List<TSBottomNavItem>, selected: String, configure: TSBottomNavView.() -> Unit = {}): TSBottomNavView =
        addSpaced(
            TSBottomNavView(context).apply {
                this.items = items
                selectedId = selected
                setOnApplyWindowInsetsListener(null)
                configure()
            },
            matchWidth = true,
        ) as TSBottomNavView

    private fun button(label: Int, tag: String, variant: TSButtonVariant = TSButtonVariant.Secondary, onClick: () -> Unit): TSButtonView =
        TSButtonView(requireContext()).apply {
            text = getString(label)
            this.variant = variant
            this.tag = tag
            setOnClickListener { onClick() }
        }

    private fun bindVariants(section: LinearLayout) {
        val context = section.context
        section.addSectionHeader(DemoR.string.demo_component_variants)
        section.addCaption(getString(DemoR.string.demo_component_three_items))
        section.addNav(context.navItems(3), "home")
        section.addCaption(getString(DemoR.string.demo_component_four_items_badges))
        section.addNav(context.navItems(4, inboxBadge = 3, alertsBadge = 120), SampleData.NAV_INBOX)
        section.addCaption(getString(DemoR.string.demo_component_five_items))
        section.addNav(context.navItems(5, inboxBadge = 3, alertsBadge = 120), "home")
    }

    private fun bindStates(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_states)
        section.addCaption(getString(DemoR.string.demo_component_disabled))
        section.addNav(section.context.navItems(4, inboxBadge = 3), "home") { isEnabled = false }
    }

    private fun bindShell(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_shell)
        section.addSpaced(
            button(DemoR.string.demo_component_open_shell, OPEN_SHELL) {
                findNavController().openDestination(requireContext(), R.id.bottom_nav_shell)
            },
        )
        section.addCaption(getString(DemoR.string.demo_component_shell_caption))
    }

    private fun bindPlayground(section: LinearLayout) {
        val context = section.context
        section.addSectionHeader(DemoR.string.demo_component_playground)
        val nav = TSBottomNavView(context).apply {
            items = context.navItems(itemCount, inboxBadge = badge)
            selectedId = "home"
            tag = PLAYGROUND_NAV
            setOnApplyWindowInsetsListener(null)
        }
        val refresh = { nav.items = context.navItems(itemCount, inboxBadge = badge) }
        section.addCaption(getString(DemoR.string.demo_component_item_count))
        section.addChoiceGroup(SampleData.navCounts, { it.toString() }, itemCount, { "demo.bottom-nav.playground.count.$it" }) {
            itemCount = it
            refresh()
        }
        section.addToggle(DemoR.string.demo_component_disabled, tag = PLAYGROUND_DISABLED) { nav.isEnabled = !it }
        val row = section.addFlowRow()
        row.addView(button(DemoR.string.demo_sample_new_message, PLAYGROUND_ADD_ONE) { badge += 1; refresh() })
        row.addView(button(DemoR.string.demo_sample_add_hundred, PLAYGROUND_ADD_HUNDRED) { badge += 100; refresh() })
        section.addSpaced(button(DemoR.string.demo_sample_mark_all_read, PLAYGROUND_CLEAR, TSButtonVariant.Ghost) { badge = 0; refresh() })
        section.addSpaced(nav, matchWidth = true)
        val value: TextView = section.addCaption(selectedCaption(nav.selectedId)).apply { tag = PLAYGROUND_VALUE }
        val counter = section.addTapCounter(PLAYGROUND_CHANGES)
        nav.setOnItemSelectedListener { _, id ->
            value.text = selectedCaption(id)
            counter.increment()
        }
    }

    companion object {
        const val OPEN_SHELL = "demo.bottom-nav.open-shell"
        const val PLAYGROUND_NAV = "demo.bottom-nav.playground.nav"
        const val PLAYGROUND_DISABLED = "demo.bottom-nav.playground.disabled"
        const val PLAYGROUND_ADD_ONE = "demo.bottom-nav.playground.add-one"
        const val PLAYGROUND_ADD_HUNDRED = "demo.bottom-nav.playground.add-hundred"
        const val PLAYGROUND_CLEAR = "demo.bottom-nav.playground.clear"
        const val PLAYGROUND_VALUE = "demo.bottom-nav.playground.value"
        const val PLAYGROUND_CHANGES = "demo.bottom-nav.playground.changes"
    }
}
