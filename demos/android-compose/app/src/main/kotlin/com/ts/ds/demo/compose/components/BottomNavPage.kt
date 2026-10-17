package com.ts.ds.demo.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.bottomnav.TSBottomNav
import com.ts.ds.compose.components.button.TSButton
import com.ts.ds.compose.components.toast.LocalTSToastState
import com.ts.ds.compose.components.toast.TSToastHost
import com.ts.ds.compose.components.toast.rememberTSToastState
import com.ts.ds.compose.theme.TSSpacing
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.icon.TSIcons
import com.ts.ds.core.logic.TSToastTone
import com.ts.ds.core.style.TSBottomNavItem
import com.ts.ds.core.style.TSBottomNavStyleSpec
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.demo.compose.shell.DemoText
import com.ts.ds.demo.compose.shell.DemoTopBar
import com.ts.ds.demo.compose.shell.OptionChip
import com.ts.ds.demo.compose.shell.OptionGroup
import com.ts.ds.demo.compose.shell.ToggleRow
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.sample.SampleData

object BottomNavPageTags {
    const val OPEN_SHELL = "demo.bottom-nav.open-shell"
    const val PLAYGROUND_NAV = "demo.bottom-nav.playground.nav"
    const val PLAYGROUND_DISABLED = "demo.bottom-nav.playground.disabled"
    const val PLAYGROUND_ADD_ONE = "demo.bottom-nav.playground.add-one"
    const val PLAYGROUND_ADD_HUNDRED = "demo.bottom-nav.playground.add-hundred"
    const val PLAYGROUND_CLEAR = "demo.bottom-nav.playground.clear"
    const val PLAYGROUND_VALUE = "demo.bottom-nav.playground.value"
    const val PLAYGROUND_CHANGES = "demo.bottom-nav.playground.changes"
    const val SHELL = "demo.bottom-nav.shell"
    const val SHELL_NAV = "demo.bottom-nav.shell.nav"

    fun count(count: Int): String = "demo.bottom-nav.playground.count.$count"

    fun shellTitle(id: String): String = "demo.bottom-nav.shell.title.$id"

    fun shellAddOne(id: String): String = "demo.bottom-nav.shell.add-one.$id"

    fun shellClear(id: String): String = "demo.bottom-nav.shell.clear.$id"

    fun shellToast(id: String): String = "demo.bottom-nav.shell.toast.$id"
}

@Composable
private fun navItems(count: Int, inboxBadge: Int? = null, alertsBadge: Int? = null): List<TSBottomNavItem> =
    SampleData.navItems(count).map {
        TSBottomNavItem(
            id = it.id,
            label = stringResource(it.labelRes),
            icon = TSIcons.fromDsName(it.iconName) ?: TSIcon.Info,
            badge = when (it.id) {
                SampleData.NAV_INBOX -> inboxBadge
                SampleData.NAV_ALERTS -> alertsBadge
                else -> null
            },
        )
    }

@Composable
private fun selectedCaption(id: String): String {
    val label = SampleData.navItems.firstOrNull { it.id == id }?.labelRes?.let { stringResource(it) } ?: id
    return stringResource(DemoR.string.demo_component_selected_value, label)
}

@Composable
private fun SampleNav(
    items: List<TSBottomNavItem>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TSBottomNav(
        items = items,
        selectedId = selected,
        onSelect = onSelect,
        label = stringResource(DemoR.string.demo_sample_main_navigation),
        enabled = enabled,
        windowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier,
    )
}

fun LazyListScope.bottomNavPage(onOpenShell: () -> Unit) {
    componentSection("bottom-nav.variants", DemoR.string.demo_component_variants) { BottomNavVariants() }
    componentSection("bottom-nav.states", DemoR.string.demo_component_states) { BottomNavStates() }
    componentSection("bottom-nav.shell", DemoR.string.demo_component_shell) { BottomNavShellEntry(onOpenShell) }
    componentSection("bottom-nav.playground", DemoR.string.demo_component_playground) { BottomNavPlayground() }
}

@Composable
private fun BottomNavVariants() {
    var three by rememberSaveable { mutableStateOf("home") }
    var four by rememberSaveable { mutableStateOf(SampleData.NAV_INBOX) }
    var five by rememberSaveable { mutableStateOf("home") }
    DemoCaption(text = stringResource(DemoR.string.demo_component_three_items))
    SampleNav(navItems(3), three, { three = it })
    DemoCaption(text = stringResource(DemoR.string.demo_component_four_items_badges))
    SampleNav(navItems(4, inboxBadge = 3, alertsBadge = 120), four, { four = it })
    DemoCaption(text = stringResource(DemoR.string.demo_component_five_items))
    SampleNav(navItems(5, inboxBadge = 3, alertsBadge = 120), five, { five = it })
}

@Composable
private fun BottomNavStates() {
    DemoCaption(text = stringResource(DemoR.string.demo_component_disabled))
    SampleNav(navItems(4, inboxBadge = 3), "home", {}, enabled = false)
}

@Composable
private fun BottomNavShellEntry(onOpenShell: () -> Unit) {
    TSButton(
        text = stringResource(DemoR.string.demo_component_open_shell),
        onClick = onOpenShell,
        variant = TSButtonVariant.Secondary,
        modifier = Modifier.testTag(BottomNavPageTags.OPEN_SHELL),
    )
    DemoCaption(text = stringResource(DemoR.string.demo_component_shell_caption))
}

@Composable
private fun BottomNavPlayground() {
    var count by rememberSaveable { mutableIntStateOf(4) }
    var disabled by rememberSaveable { mutableStateOf(false) }
    var badge by rememberSaveable { mutableIntStateOf(3) }
    var selection by rememberSaveable { mutableStateOf("home") }
    var changes by rememberSaveable { mutableIntStateOf(0) }
    val items = navItems(count, inboxBadge = badge)
    val selected = items.firstOrNull { it.id == selection }?.id ?: items.first().id

    DemoCaption(text = stringResource(DemoR.string.demo_component_item_count))
    OptionGroup {
        SampleData.navCounts.forEach { option ->
            OptionChip(
                label = option.toString(),
                selected = count == option,
                onClick = { count = option },
                modifier = Modifier.testTag(BottomNavPageTags.count(option)),
            )
        }
    }
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_disabled),
        checked = disabled,
        onCheckedChange = { disabled = it },
        modifier = Modifier.testTag(BottomNavPageTags.PLAYGROUND_DISABLED),
    )
    DemoComponentRow {
        TSButton(
            text = stringResource(DemoR.string.demo_sample_new_message),
            onClick = { badge += 1 },
            variant = TSButtonVariant.Secondary,
            modifier = Modifier.testTag(BottomNavPageTags.PLAYGROUND_ADD_ONE),
        )
        TSButton(
            text = stringResource(DemoR.string.demo_sample_add_hundred),
            onClick = { badge += 100 },
            variant = TSButtonVariant.Secondary,
            modifier = Modifier.testTag(BottomNavPageTags.PLAYGROUND_ADD_HUNDRED),
        )
    }
    TSButton(
        text = stringResource(DemoR.string.demo_sample_mark_all_read),
        onClick = { badge = 0 },
        variant = TSButtonVariant.Ghost,
        modifier = Modifier.testTag(BottomNavPageTags.PLAYGROUND_CLEAR),
    )
    SampleNav(
        items = items,
        selected = selected,
        onSelect = {
            selection = it
            changes++
        },
        enabled = !disabled,
        modifier = Modifier.testTag(BottomNavPageTags.PLAYGROUND_NAV),
    )
    DemoCaption(text = selectedCaption(selected), modifier = Modifier.testTag(BottomNavPageTags.PLAYGROUND_VALUE))
    DemoTapCounter(count = changes, tag = BottomNavPageTags.PLAYGROUND_CHANGES)
}

@Composable
fun BottomNavShellScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    var selection by rememberSaveable { mutableStateOf("home") }
    var inboxBadge by rememberSaveable { mutableIntStateOf(3) }
    val items = navItems(4, inboxBadge = inboxBadge)
    TSToastHost(
        state = rememberTSToastState(),
        modifier = modifier
            .fillMaxSize()
            .testTag(BottomNavPageTags.SHELL),
        bottomInset = TSBottomNavStyleSpec.container.heightDp.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TSTheme.colors.backgroundPrimary),
        ) {
            DemoTopBar(title = stringResource(DemoR.string.demo_component_shell), onBack = onBack)
            Box(modifier = Modifier.weight(1f)) {
                items.firstOrNull { it.id == selection }?.let { item ->
                    BottomNavShellTab(
                        item = item,
                        onAddOne = { inboxBadge += 1 },
                        onClear = { inboxBadge = 0 },
                    )
                }
            }
            TSBottomNav(
                items = items,
                selectedId = selection,
                onSelect = { selection = it },
                label = stringResource(DemoR.string.demo_sample_main_navigation),
                modifier = Modifier.testTag(BottomNavPageTags.SHELL_NAV),
            )
        }
    }
}

@Composable
private fun BottomNavShellTab(item: TSBottomNavItem, onAddOne: () -> Unit, onClear: () -> Unit) {
    val toasts = LocalTSToastState.current
    val sent = stringResource(DemoR.string.demo_sample_message_sent)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(TSSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(TSSpacing.space4),
    ) {
        DemoText(
            text = item.label,
            role = TSTypeRole.Title2,
            modifier = Modifier
                .testTag(BottomNavPageTags.shellTitle(item.id))
                .semantics { heading() },
        )
        DemoText(text = stringResource(DemoR.string.demo_sample_shell_body), color = TSColorRole.TextSecondary)
        TSButton(
            text = stringResource(DemoR.string.demo_sample_new_message),
            onClick = onAddOne,
            variant = TSButtonVariant.Secondary,
            modifier = Modifier.testTag(BottomNavPageTags.shellAddOne(item.id)),
        )
        TSButton(
            text = stringResource(DemoR.string.demo_sample_mark_all_read),
            onClick = onClear,
            variant = TSButtonVariant.Ghost,
            modifier = Modifier.testTag(BottomNavPageTags.shellClear(item.id)),
        )
        TSButton(
            text = stringResource(DemoR.string.demo_component_show_toast),
            onClick = { toasts?.show(sent, TSToastTone.Success) },
            variant = TSButtonVariant.Secondary,
            modifier = Modifier.testTag(BottomNavPageTags.shellToast(item.id)),
        )
    }
}
