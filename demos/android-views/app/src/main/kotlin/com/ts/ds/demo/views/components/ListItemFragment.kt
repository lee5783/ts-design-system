package com.ts.ds.demo.views.components

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.icon.TSIcons
import com.ts.ds.core.style.TSBadgeTone
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.sample.SampleData
import com.ts.ds.demo.support.sample.SampleListRow
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.views.components.listitem.TSListItemLeading
import com.ts.ds.views.components.listitem.TSListItemTrailing
import com.ts.ds.views.components.listitem.TSListItemView
import com.ts.ds.views.components.listitem.TSListView

class ListItemFragment : DemoPageFragment(R.layout.page_list_item) {
    override val page: DemoPageId = DemoPageId.ListItem

    private enum class LeadingOption(val labelRes: Int) {
        None(DemoR.string.demo_component_none),
        Icon(DemoR.string.demo_component_icon),
        Person(DemoR.string.demo_component_person),
    }

    private enum class TrailingOption(val labelRes: Int) {
        None(DemoR.string.demo_component_none),
        Text(DemoR.string.demo_component_text),
        Badge(DemoR.string.demo_component_badge),
        Switch(DemoR.string.demo_component_switch),
    }

    private var playgroundLeading = LeadingOption.None
    private var playgroundTrailing = TrailingOption.None
    private var playgroundSubtitle = false
    private var playgroundChevron = false

    override fun bindRows(rows: LinearLayout) {
        rows.markSectionHeadings()
        bindContents(rows.findViewById(R.id.list_item_contents))
        bindStates(rows.findViewById(R.id.list_item_states))
        rows.findViewById<TSListItemView>(R.id.list_item_truncation_row).setOnClickListener {}
        bindPlayground(rows.findViewById(R.id.list_item_playground))
    }

    private val SampleListRow.icon: TSIcon
        get() = TSIcons.fromDsName(iconName) ?: TSIcon.Info

    private fun stateText(on: Boolean): String = getString(if (on) DemoR.string.demo_state_on else DemoR.string.demo_state_off)

    private fun ViewGroup.addList(vararg items: TSListItemView): TSListView =
        addSpaced(TSListView(context).apply { items.forEach { addView(it) } }, matchWidth = true) as TSListView

    private fun item(title: CharSequence, configure: TSListItemView.() -> Unit = {}): TSListItemView =
        TSListItemView(requireContext()).apply {
            this.title = title
            configure()
        }

    private fun bindContents(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_contents)
        val inbox = SampleData.listRow("inbox")
        val calendar = SampleData.listRow("calendar")
        val person = SampleData.people.first()
        lateinit var wifiValue: TextView
        section.addList(
            item(getString(SampleData.listRow("favorites").titleRes)),
            item(getString(inbox.titleRes)) {
                subtitle = inbox.subtitleRes?.let(::getString)
                leading = TSListItemLeading.Icon(inbox.icon)
                trailing = TSListItemTrailing.Badge(getString(DemoR.string.demo_sample_new), TSBadgeTone.Accent)
                showsChevron = true
                tag = INBOX
                setOnClickListener {}
            },
            item(getString(calendar.titleRes)) {
                subtitle = calendar.subtitleRes?.let(::getString)
                leading = TSListItemLeading.Icon(calendar.icon)
                trailing = TSListItemTrailing.Text(getString(DemoR.string.demo_sample_today))
            },
            item(person.name) {
                subtitle = person.email
                leading = TSListItemLeading.Avatar(person.name, demoSamplePhoto(context))
                trailing = TSListItemTrailing.Text(getString(DemoR.string.demo_sample_online))
            },
            item(getString(DemoR.string.demo_sample_wifi)) {
                leading = TSListItemLeading.Icon(TSIcon.Settings)
                id = R.id.list_item_wifi_row
                trailing = TSListItemTrailing.Switch
                isChecked = true
                tag = WIFI
                setOnCheckedChangeListener { _, on -> wifiValue.text = stateText(on) }
            },
            item(getString(SampleData.listRow("settings").titleRes)) {
                showsChevron = true
                setOnClickListener {}
            },
        )
        wifiValue = section.addCaption(stateText(true)).apply { tag = WIFI_VALUE }
    }

    private fun bindStates(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_states)
        lateinit var counter: DemoTapCounter
        val countTap = View.OnClickListener { counter.increment() }
        section.addList(
            item(getString(DemoR.string.demo_component_default)) {
                showsChevron = true
                tag = STATE_DEFAULT
                setOnClickListener(countTap)
            },
            item(getString(DemoR.string.demo_component_disabled)) {
                showsChevron = true
                tag = STATE_DISABLED
                setOnClickListener(countTap)
                isEnabled = false
            },
            item(getString(DemoR.string.demo_component_no_action)) {
                subtitle = getString(SampleData.listRow("favorites").titleRes)
                tag = STATE_STATIC
            },
            item(getString(DemoR.string.demo_sample_dark_mode)) {
                trailing = TSListItemTrailing.Switch
                isEnabled = false
            },
        )
        counter = section.addTapCounter(STATE_COUNT)
    }

    private fun bindPlayground(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_playground)
        val row = item(getString(SampleData.listRow("inbox").titleRes)) {
            id = R.id.list_item_playground_row
            tag = PLAYGROUND_ROW
        }
        section.addToggle(DemoR.string.demo_component_subtitle, tag = PLAYGROUND_SUBTITLE) {
            playgroundSubtitle = it
            configurePlayground(row)
        }
        section.addToggle(DemoR.string.demo_component_chevron, tag = PLAYGROUND_CHEVRON) {
            playgroundChevron = it
            configurePlayground(row)
        }
        section.addToggle(DemoR.string.demo_component_disabled, tag = PLAYGROUND_DISABLED) { row.isEnabled = !it }
        section.addCaption(getString(DemoR.string.demo_component_leading))
        section.addChoiceGroup(LeadingOption.entries, { getString(it.labelRes) }, playgroundLeading, { "demo.list-item.playground.leading.${it.name.lowercase()}" }) {
            playgroundLeading = it
            configurePlayground(row)
        }
        section.addCaption(getString(DemoR.string.demo_component_trailing))
        section.addChoiceGroup(TrailingOption.entries, { getString(it.labelRes) }, playgroundTrailing, { "demo.list-item.playground.trailing.${it.name.lowercase()}" }) {
            playgroundTrailing = it
            configurePlayground(row)
        }
        section.addList(row)
        val value = section.addCaption(stateText(false)).apply { tag = PLAYGROUND_VALUE }
        val counter = section.addTapCounter(PLAYGROUND_COUNT)
        row.setOnCheckedChangeListener { _, on -> value.text = stateText(on) }
        row.setOnClickListener { counter.increment() }
        configurePlayground(row)
    }

    private fun configurePlayground(row: TSListItemView) {
        val inbox = SampleData.listRow("inbox")
        row.subtitle = if (playgroundSubtitle) inbox.subtitleRes?.let(::getString) else null
        row.showsChevron = playgroundChevron && playgroundTrailing != TrailingOption.Switch
        row.leading = when (playgroundLeading) {
            LeadingOption.None -> null
            LeadingOption.Icon -> TSListItemLeading.Icon(inbox.icon)
            LeadingOption.Person -> TSListItemLeading.Avatar(SampleData.people.first().name)
        }
        row.trailing = when (playgroundTrailing) {
            TrailingOption.None -> null
            TrailingOption.Text -> TSListItemTrailing.Text(getString(DemoR.string.demo_sample_today))
            TrailingOption.Badge -> TSListItemTrailing.Badge(getString(DemoR.string.demo_sample_new), TSBadgeTone.Accent)
            TrailingOption.Switch -> TSListItemTrailing.Switch
        }
    }

    companion object {
        const val INBOX = "demo.list-item.inbox"
        const val WIFI = "demo.list-item.wifi"
        const val WIFI_VALUE = "demo.list-item.wifi.value"
        const val STATE_DEFAULT = "demo.list-item.state.default"
        const val STATE_DISABLED = "demo.list-item.state.disabled"
        const val STATE_STATIC = "demo.list-item.state.static"
        const val STATE_COUNT = "demo.list-item.state.count"
        const val PLAYGROUND_SUBTITLE = "demo.list-item.playground.subtitle"
        const val PLAYGROUND_CHEVRON = "demo.list-item.playground.chevron"
        const val PLAYGROUND_DISABLED = "demo.list-item.playground.disabled"
        const val PLAYGROUND_ROW = "demo.list-item.playground.row"
        const val PLAYGROUND_VALUE = "demo.list-item.playground.value"
        const val PLAYGROUND_COUNT = "demo.list-item.playground.count"
    }
}
