package com.ts.ds.compose.snapshots

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.ts.ds.compose.components.shared.LocalTSForcedControlState
import com.ts.ds.compose.components.tabs.TSTabs
import com.ts.ds.core.style.TSControlState
import com.ts.ds.core.style.TSTabItem
import com.ts.ds.core.style.TSTabsVariant

private val sampleRanges = listOf("Day", "Week", "Month", "Year").map { TSTabItem(it.lowercase(), it) }
private val sampleSections = listOf(
    "Overview", "Activity", "Members", "Billing", "Settings", "Integrations", "Security", "Notifications", "Reports",
).map { TSTabItem(it.lowercase(), it) }

@Composable
private fun TabsStates(variant: TSTabsVariant, block: Boolean) {
    val items = sampleRanges.take(3)
    TSTabs(items, "week", {}, variant = variant, block = block)
    CompositionLocalProvider(LocalTSForcedControlState provides TSControlState.Pressed) {
        TSTabs(items, "week", {}, variant = variant, block = block)
    }
    TSTabs(items, "week", {}, variant = variant, block = block, enabled = false)
    CompositionLocalProvider(LocalTSForcedControlState provides TSControlState.Focused) {
        TSTabs(items, "week", {}, variant = variant, block = block)
    }
}

@Composable
internal fun UnderlineTabsSample() {
    SampleColumn {
        TabsStates(TSTabsVariant.Underline, block = false)
        TSTabs(sampleSections, "overview", {})
    }
}

@Composable
internal fun SegmentedTabsSample() {
    SampleColumn {
        TabsStates(TSTabsVariant.Segmented, block = false)
        TSTabs(sampleRanges, "month", {}, variant = TSTabsVariant.Segmented, block = true)
        TSTabs(listOf(TSTabItem("grid", "Grid"), TSTabItem("list", "List")), "grid", {}, variant = TSTabsVariant.Segmented)
    }
}
