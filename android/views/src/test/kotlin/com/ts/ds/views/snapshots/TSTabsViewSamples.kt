package com.ts.ds.views.snapshots

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ts.ds.core.style.TSTabItem
import com.ts.ds.core.style.TSTabsVariant
import com.ts.ds.views.components.tabs.TSTabsView

private val sampleRanges = listOf("Day", "Week", "Month", "Year").map { TSTabItem(it.lowercase(), it) }
private val sampleSections = listOf(
    "Overview", "Activity", "Members", "Billing", "Settings", "Integrations", "Security", "Notifications", "Reports",
).map { TSTabItem(it.lowercase(), it) }

private enum class TabsSampleState { Default, Pressed, Disabled, Focused }

private val fillWidth = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

private fun Context.sampleTabs(variant: TSTabsVariant, items: List<TSTabItem>, selected: String, block: Boolean = false): TSTabsView =
    TSTabsView(this).apply {
        this.items = items
        this.variant = variant
        isBlock = block
        selectedId = selected
        reduceMotion = { true }
    }

private fun LinearLayout.addTabsStates(context: Context, variant: TSTabsVariant) {
    TabsSampleState.entries.forEach { state ->
        val view = context.sampleTabs(variant, sampleRanges.take(3), "week")
        view.isEnabled = state != TabsSampleState.Disabled
        addView(view, LinearLayout.LayoutParams(fillWidth))
        when (state) {
            TabsSampleState.Pressed -> view.tabs.filter { !it.isTabSelected }.forEach { it.isPressed = true }
            TabsSampleState.Focused -> view.tabs.first().apply {
                isFocusableInTouchMode = true
                post { requestFocus() }
            }
            else -> Unit
        }
    }
}

internal fun underlineTabsSample(context: Context): View = context.galleryColumn().apply {
    addTabsStates(context, TSTabsVariant.Underline)
    addView(context.sampleTabs(TSTabsVariant.Underline, sampleSections, "overview"), LinearLayout.LayoutParams(fillWidth))
}

internal fun segmentedTabsSample(context: Context): View = context.galleryColumn().apply {
    addTabsStates(context, TSTabsVariant.Segmented)
    addView(context.sampleTabs(TSTabsVariant.Segmented, sampleRanges, "month", block = true), LinearLayout.LayoutParams(fillWidth))
    addView(
        context.sampleTabs(TSTabsVariant.Segmented, listOf(TSTabItem("grid", "Grid"), TSTabItem("list", "List")), "grid"),
        LinearLayout.LayoutParams(fillWidth),
    )
}
