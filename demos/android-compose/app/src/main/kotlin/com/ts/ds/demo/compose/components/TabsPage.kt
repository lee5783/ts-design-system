package com.ts.ds.demo.compose.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.ts.ds.compose.components.tabs.TSTabs
import com.ts.ds.core.style.TSTabItem
import com.ts.ds.core.style.TSTabsVariant
import com.ts.ds.demo.compose.shell.OptionChip
import com.ts.ds.demo.compose.shell.OptionGroup
import com.ts.ds.demo.compose.shell.ToggleRow
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.sample.SampleData
import com.ts.ds.demo.support.sample.SampleTab

object TabsPageTags {
    const val OVERFLOW = "demo.tabs.overflow"
    const val OVERFLOW_VALUE = "demo.tabs.overflow.value"
    const val PLAYGROUND_TABS = "demo.tabs.playground.tabs"
    const val PLAYGROUND_DISABLED = "demo.tabs.playground.disabled"
    const val PLAYGROUND_BLOCK = "demo.tabs.playground.block"
    const val PLAYGROUND_VALUE = "demo.tabs.playground.value"
    const val PLAYGROUND_COUNT = "demo.tabs.playground.count"

    fun variant(variant: TSTabsVariant): String = "demo.tabs.playground.variant.${variant.name.lowercase()}"
}

@Composable
private fun List<SampleTab>.tabItems(): List<TSTabItem> = map { TSTabItem(it.id, stringResource(it.labelRes)) }

@Composable
private fun selectedCaption(items: List<TSTabItem>, id: String): String =
    stringResource(DemoR.string.demo_component_selected_value, items.firstOrNull { it.id == id }?.label ?: id)

@Composable
private fun variantName(variant: TSTabsVariant): String = stringResource(
    when (variant) {
        TSTabsVariant.Underline -> DemoR.string.demo_component_underline
        TSTabsVariant.Segmented -> DemoR.string.demo_component_segmented
    },
)

fun LazyListScope.tabsPage() {
    componentSection("tabs.variants", DemoR.string.demo_component_variants) { TabsVariants() }
    componentSection("tabs.block", DemoR.string.demo_component_block) { TabsBlock() }
    componentSection("tabs.states", DemoR.string.demo_component_states) { TabsStates() }
    componentSection("tabs.overflow", DemoR.string.demo_component_overflow) { TabsOverflow() }
    componentSection("tabs.playground", DemoR.string.demo_component_playground) { TabsPlayground() }
}

@Composable
private fun TabsVariants() {
    var section by rememberSaveable { mutableStateOf(SampleData.tabSections.first().id) }
    var range by rememberSaveable { mutableStateOf(SampleData.tabRanges[1].id) }
    DemoCaption(text = variantName(TSTabsVariant.Underline))
    TSTabs(SampleData.tabSections.tabItems(), section, { section = it }, label = stringResource(DemoR.string.demo_sample_project_sections))
    DemoCaption(text = variantName(TSTabsVariant.Segmented))
    TSTabs(
        SampleData.tabRanges.take(3).tabItems(),
        range,
        { range = it },
        variant = TSTabsVariant.Segmented,
        label = stringResource(DemoR.string.demo_sample_time_range),
    )
}

@Composable
private fun TabsBlock() {
    var range by rememberSaveable { mutableStateOf(SampleData.tabRanges.first().id) }
    var layout by rememberSaveable { mutableStateOf(SampleData.tabLayouts.first().id) }
    TSTabs(
        SampleData.tabRanges.tabItems(),
        range,
        { range = it },
        variant = TSTabsVariant.Segmented,
        block = true,
        label = stringResource(DemoR.string.demo_sample_time_range),
    )
    TSTabs(SampleData.tabLayouts.tabItems(), layout, { layout = it }, variant = TSTabsVariant.Segmented)
}

@Composable
private fun TabsStates() {
    TSTabsVariant.entries.forEach { variant ->
        DemoCaption(text = stringResource(DemoR.string.demo_component_value_disabled, variantName(variant)))
        TSTabs(SampleData.tabRanges.take(3).tabItems(), SampleData.tabRanges[1].id, {}, variant = variant, block = true, enabled = false)
    }
}

@Composable
private fun TabsOverflow() {
    val items = SampleData.tabManySections.tabItems()
    var section by rememberSaveable { mutableStateOf(items.first().id) }
    TSTabs(
        items,
        section,
        { section = it },
        label = stringResource(DemoR.string.demo_sample_project_sections),
        modifier = Modifier.testTag(TabsPageTags.OVERFLOW),
    )
    DemoCaption(text = selectedCaption(items, section), modifier = Modifier.testTag(TabsPageTags.OVERFLOW_VALUE))
}

@Composable
private fun TabsPlayground() {
    val items = SampleData.tabRanges.tabItems()
    var variant by rememberSaveable { mutableStateOf(TSTabsVariant.Segmented) }
    var disabled by rememberSaveable { mutableStateOf(false) }
    var block by rememberSaveable { mutableStateOf(true) }
    var selection by rememberSaveable { mutableStateOf(items.first().id) }
    var changes by rememberSaveable { mutableIntStateOf(0) }

    DemoCaption(text = stringResource(DemoR.string.demo_component_variant))
    OptionGroup {
        TSTabsVariant.entries.forEach { option ->
            OptionChip(
                label = option.name.lowercase(),
                selected = variant == option,
                onClick = { variant = option },
                modifier = Modifier.testTag(TabsPageTags.variant(option)),
            )
        }
    }
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_disabled),
        checked = disabled,
        onCheckedChange = { disabled = it },
        modifier = Modifier.testTag(TabsPageTags.PLAYGROUND_DISABLED),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_block),
        checked = block,
        onCheckedChange = { block = it },
        modifier = Modifier.testTag(TabsPageTags.PLAYGROUND_BLOCK),
    )
    TSTabs(
        items = items,
        selectedId = selection,
        onSelect = {
            selection = it
            changes++
        },
        variant = variant,
        block = block,
        label = stringResource(DemoR.string.demo_sample_time_range),
        enabled = !disabled,
        modifier = Modifier.testTag(TabsPageTags.PLAYGROUND_TABS),
    )
    DemoCaption(text = selectedCaption(items, selection), modifier = Modifier.testTag(TabsPageTags.PLAYGROUND_VALUE))
    DemoTapCounter(count = changes, tag = TabsPageTags.PLAYGROUND_COUNT)
}
