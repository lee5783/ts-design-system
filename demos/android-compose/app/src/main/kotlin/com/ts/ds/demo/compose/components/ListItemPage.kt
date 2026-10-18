package com.ts.ds.demo.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.avatar.TSAvatar
import com.ts.ds.compose.components.badge.TSBadge
import com.ts.ds.compose.components.listitem.TSDisclosureListItem
import com.ts.ds.compose.components.listitem.TSDivider
import com.ts.ds.compose.components.listitem.TSList
import com.ts.ds.compose.components.listitem.TSListItem
import com.ts.ds.compose.components.listitem.TSListItemIcon
import com.ts.ds.compose.components.listitem.TSListItemValue
import com.ts.ds.compose.components.text.TSText
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.icon.TSIcons
import com.ts.ds.core.style.TSBadgeTone
import com.ts.ds.core.style.TSTextColor
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.demo.compose.shell.OptionChip
import com.ts.ds.demo.compose.shell.OptionGroup
import com.ts.ds.demo.compose.shell.ToggleRow
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.sample.SampleData
import com.ts.ds.demo.support.sample.SampleListRow

object ListItemPageTags {
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
    const val LAZY_DIVIDER = "demo.list-item.lazy-divider"
    const val DISCLOSURE_BODY = "demo.list-item.disclosure.body"

    fun disclosure(id: String): String = "demo.list-item.disclosure.$id"

    fun leading(option: ListItemLeadingOption): String = "demo.list-item.playground.leading.${option.name.lowercase()}"

    fun trailing(option: ListItemTrailingOption): String = "demo.list-item.playground.trailing.${option.name.lowercase()}"
}

enum class ListItemLeadingOption(val labelRes: Int) {
    None(DemoR.string.demo_component_none),
    Icon(DemoR.string.demo_component_icon),
    Person(DemoR.string.demo_component_person),
}

enum class ListItemTrailingOption(val labelRes: Int) {
    None(DemoR.string.demo_component_none),
    Text(DemoR.string.demo_component_text),
    Badge(DemoR.string.demo_component_badge),
    Switch(DemoR.string.demo_component_switch),
}

private val SampleListRow.icon: TSIcon
    get() = TSIcons.fromDsName(iconName) ?: TSIcon.Info

@Composable
private fun SampleListRow.title(): String = stringResource(titleRes)

@Composable
private fun SampleListRow.subtitle(): String? = subtitleRes?.let { stringResource(it) }

@Composable
private fun stateText(on: Boolean): String = stringResource(if (on) DemoR.string.demo_state_on else DemoR.string.demo_state_off)

fun LazyListScope.listItemPage() {
    componentSection("list-item.contents", DemoR.string.demo_component_contents) { ListItemContents() }
    componentSection("list-item.states", DemoR.string.demo_component_states) { ListItemStates() }
    componentSection("list-item.lazy-divider", DemoR.string.demo_component_lazy_divider) { ListItemLazyDivider() }
    componentSection("list-item.disclosure", DemoR.string.demo_component_disclosure) { ListItemDisclosure() }
    componentSection("list-item.truncation", DemoR.string.demo_component_truncation) { ListItemTruncation() }
    componentSection("list-item.playground", DemoR.string.demo_component_playground) { ListItemPlayground() }
}

@Composable
private fun ListItemContents() {
    var wifi by rememberSaveable { mutableStateOf(true) }
    val inbox = SampleData.listRow("inbox")
    val calendar = SampleData.listRow("calendar")
    val settings = SampleData.listRow("settings")
    val person = SampleData.people.first()
    TSList {
        TSListItem(SampleData.listRow("favorites").title())
        TSListItem(
            inbox.title(),
            subtitle = inbox.subtitle(),
            onClick = {},
            showsChevron = true,
            modifier = Modifier.testTag(ListItemPageTags.INBOX),
            leading = { TSListItemIcon(inbox.icon) },
        ) {
            TSBadge(stringResource(DemoR.string.demo_sample_new), tone = TSBadgeTone.Accent)
        }
        TSListItem(calendar.title(), subtitle = calendar.subtitle(), leading = { TSListItemIcon(calendar.icon) }) {
            TSListItemValue(stringResource(DemoR.string.demo_sample_today))
        }
        TSListItem(person.name, subtitle = person.email, leading = { TSAvatar(person.name, image = rememberDemoSamplePhoto()) }) {
            TSListItemValue(stringResource(DemoR.string.demo_sample_online))
        }
        TSListItem(
            stringResource(DemoR.string.demo_sample_wifi),
            checked = wifi,
            onCheckedChange = { wifi = it },
            modifier = Modifier.testTag(ListItemPageTags.WIFI),
            leading = { TSListItemIcon(TSIcon.Settings) },
        )
        TSListItem(settings.title(), onClick = {}, showsChevron = true)
    }
    DemoCaption(text = stateText(wifi), modifier = Modifier.testTag(ListItemPageTags.WIFI_VALUE))
}

@Composable
private fun ListItemStates() {
    var taps by rememberSaveable { mutableIntStateOf(0) }
    var darkMode by rememberSaveable { mutableStateOf(false) }
    TSList {
        TSListItem(
            stringResource(DemoR.string.demo_component_default),
            onClick = { taps++ },
            showsChevron = true,
            modifier = Modifier.testTag(ListItemPageTags.STATE_DEFAULT),
        )
        TSListItem(
            stringResource(DemoR.string.demo_component_disabled),
            onClick = { taps++ },
            showsChevron = true,
            enabled = false,
            modifier = Modifier.testTag(ListItemPageTags.STATE_DISABLED),
        )
        TSListItem(
            stringResource(DemoR.string.demo_component_no_action),
            subtitle = SampleData.listRow("favorites").title(),
            modifier = Modifier.testTag(ListItemPageTags.STATE_STATIC),
        )
        TSListItem(stringResource(DemoR.string.demo_sample_dark_mode), checked = darkMode, onCheckedChange = { darkMode = it }, enabled = false)
    }
    DemoTapCounter(count = taps, tag = ListItemPageTags.STATE_COUNT)
}

@Composable
private fun ListItemLazyDivider() {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LAZY_DIVIDER_MAX_HEIGHT)
            .background(TSTheme.colors.surfaceCard)
            .testTag(ListItemPageTags.LAZY_DIVIDER),
        userScrollEnabled = false,
    ) {
        itemsIndexed(SampleData.listRows, key = { _, row -> row.id }) { index, row ->
            if (index > 0) TSDivider()
            TSListItem(row.title(), subtitle = row.subtitle())
        }
    }
}

private val LAZY_DIVIDER_MAX_HEIGHT = 480.dp

private class LicenceSample(val id: String, val titleRes: Int, val licenceRes: Int, val bodyRes: Int)

private val licenceSamples = listOf(
    LicenceSample("kotlin", DemoR.string.demo_sample_licence_kotlin, DemoR.string.demo_sample_licence_apache, DemoR.string.demo_sample_licence_apache_body),
    LicenceSample("snapshot", DemoR.string.demo_sample_licence_snapshot, DemoR.string.demo_sample_licence_mit, DemoR.string.demo_sample_licence_mit_body),
)

@Composable
private fun ListItemDisclosure() {
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    TSList {
        licenceSamples.forEach { licence ->
            TSDisclosureListItem(
                title = stringResource(licence.titleRes),
                expanded = expanded == licence.id,
                onExpandedChange = { expanded = if (it) licence.id else null },
                subtitle = stringResource(licence.licenceRes),
                modifier = Modifier.testTag(ListItemPageTags.disclosure(licence.id)),
            ) {
                TSText(
                    stringResource(licence.bodyRes),
                    Modifier.testTag(ListItemPageTags.DISCLOSURE_BODY),
                    role = TSTypeRole.BodySm,
                    color = TSTextColor.Secondary,
                )
            }
        }
    }
}

@Composable
private fun ListItemTruncation() {
    val downloads = SampleData.listRow("downloads")
    TSList {
        TSListItem(
            stringResource(DemoR.string.demo_sample_long_setting),
            subtitle = downloads.subtitle(),
            onClick = {},
            showsChevron = true,
            leading = { TSListItemIcon(downloads.icon) },
        ) {
            TSListItemValue(stringResource(DemoR.string.demo_sample_today))
        }
    }
}

@Composable
private fun ListItemPlayground() {
    var showsSubtitle by rememberSaveable { mutableStateOf(false) }
    var showsChevron by rememberSaveable { mutableStateOf(false) }
    var disabled by rememberSaveable { mutableStateOf(false) }
    var leading by rememberSaveable { mutableStateOf(ListItemLeadingOption.None) }
    var trailing by rememberSaveable { mutableStateOf(ListItemTrailingOption.None) }
    var on by rememberSaveable { mutableStateOf(false) }
    var taps by rememberSaveable { mutableIntStateOf(0) }

    ToggleRow(
        label = stringResource(DemoR.string.demo_component_subtitle),
        checked = showsSubtitle,
        onCheckedChange = { showsSubtitle = it },
        modifier = Modifier.testTag(ListItemPageTags.PLAYGROUND_SUBTITLE),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_chevron),
        checked = showsChevron,
        onCheckedChange = { showsChevron = it },
        modifier = Modifier.testTag(ListItemPageTags.PLAYGROUND_CHEVRON),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_disabled),
        checked = disabled,
        onCheckedChange = { disabled = it },
        modifier = Modifier.testTag(ListItemPageTags.PLAYGROUND_DISABLED),
    )
    DemoCaption(text = stringResource(DemoR.string.demo_component_leading))
    OptionGroup {
        ListItemLeadingOption.entries.forEach { option ->
            OptionChip(
                label = stringResource(option.labelRes),
                selected = leading == option,
                onClick = { leading = option },
                modifier = Modifier.testTag(ListItemPageTags.leading(option)),
            )
        }
    }
    DemoCaption(text = stringResource(DemoR.string.demo_component_trailing))
    OptionGroup {
        ListItemTrailingOption.entries.forEach { option ->
            OptionChip(
                label = stringResource(option.labelRes),
                selected = trailing == option,
                onClick = { trailing = option },
                modifier = Modifier.testTag(ListItemPageTags.trailing(option)),
            )
        }
    }
    TSList {
        PlaygroundRow(
            showsSubtitle = showsSubtitle,
            showsChevron = showsChevron,
            enabled = !disabled,
            leading = leading,
            trailing = trailing,
            checked = on,
            onCheckedChange = { on = it },
            onClick = { taps++ },
        )
    }
    DemoCaption(text = stateText(on), modifier = Modifier.testTag(ListItemPageTags.PLAYGROUND_VALUE))
    DemoTapCounter(count = taps, tag = ListItemPageTags.PLAYGROUND_COUNT)
}

@Composable
private fun PlaygroundRow(
    showsSubtitle: Boolean,
    showsChevron: Boolean,
    enabled: Boolean,
    leading: ListItemLeadingOption,
    trailing: ListItemTrailingOption,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val inbox = SampleData.listRow("inbox")
    val title = inbox.title()
    val subtitle = if (showsSubtitle) inbox.subtitle() else null
    val leadingContent: (@Composable () -> Unit)? = when (leading) {
        ListItemLeadingOption.None -> null
        ListItemLeadingOption.Icon -> { { TSListItemIcon(inbox.icon) } }
        ListItemLeadingOption.Person -> { { TSAvatar(SampleData.people.first().name) } }
    }
    val modifier = Modifier.testTag(ListItemPageTags.PLAYGROUND_ROW)
    when (trailing) {
        ListItemTrailingOption.Switch -> TSListItem(
            title,
            checked = checked,
            onCheckedChange = onCheckedChange,
            subtitle = subtitle,
            enabled = enabled,
            modifier = modifier,
            leading = leadingContent,
        )
        ListItemTrailingOption.None, ListItemTrailingOption.Text, ListItemTrailingOption.Badge -> TSListItem(
            title,
            subtitle = subtitle,
            onClick = onClick,
            showsChevron = showsChevron,
            enabled = enabled,
            modifier = modifier,
            leading = leadingContent,
            trailing = when (trailing) {
                ListItemTrailingOption.Text -> { { TSListItemValue(stringResource(DemoR.string.demo_sample_today)) } }
                ListItemTrailingOption.Badge -> { { TSBadge(stringResource(DemoR.string.demo_sample_new), tone = TSBadgeTone.Accent) } }
                ListItemTrailingOption.None, ListItemTrailingOption.Switch -> null
            },
        )
    }
}
