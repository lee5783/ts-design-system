package com.ts.ds.compose.snapshots

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.ts.ds.compose.components.bottomnav.TSBottomNav
import com.ts.ds.compose.components.shared.LocalTSForcedControlState
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSBottomNavItem
import com.ts.ds.core.style.TSControlState

private val sampleItems = listOf(
    TSBottomNavItem("home", "Home", TSIcon.Home),
    TSBottomNavItem("inbox", "Inbox", TSIcon.Mail),
    TSBottomNavItem("search", "Search", TSIcon.Search),
    TSBottomNavItem("alerts", "Alerts", TSIcon.Bell),
    TSBottomNavItem("profile", "Profile", TSIcon.User),
)

private fun items(count: Int, inbox: Int? = null, alerts: Int? = null): List<TSBottomNavItem> {
    val badged = sampleItems.map {
        when (it.id) {
            "inbox" -> it.badged(inbox)
            "alerts" -> it.badged(alerts)
            else -> it
        }
    }
    return when (count) {
        3 -> listOf(badged[0], badged[1], badged[4])
        4 -> badged.take(4)
        else -> badged
    }
}

@Composable
private fun SampleNav(items: List<TSBottomNavItem>, selected: String, enabled: Boolean = true) {
    TSBottomNav(items, selected, {}, label = "Main navigation", enabled = enabled, windowInsets = WindowInsets(0, 0, 0, 0))
}

@Composable
internal fun BottomNavGallerySample() {
    SampleColumn {
        SampleNav(items(3), "home")
        SampleNav(items(4, inbox = 3, alerts = 120), "inbox")
        CompositionLocalProvider(LocalTSForcedControlState provides TSControlState.Pressed) {
            SampleNav(items(5), "home")
        }
        CompositionLocalProvider(LocalTSForcedControlState provides TSControlState.Focused) {
            SampleNav(items(5, inbox = 3, alerts = 120), "alerts")
        }
        SampleNav(items(4, inbox = 3), "home", enabled = false)
    }
}
