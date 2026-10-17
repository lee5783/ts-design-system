package com.ts.ds.compose.snapshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.avatar.TSAvatar
import com.ts.ds.compose.components.badge.TSBadge
import com.ts.ds.compose.components.listitem.TSList
import com.ts.ds.compose.components.listitem.TSListItem
import com.ts.ds.compose.components.listitem.TSListItemIcon
import com.ts.ds.compose.components.listitem.TSListItemValue
import com.ts.ds.compose.components.listitem.TSListSection
import com.ts.ds.compose.components.shared.LocalTSForcedControlState
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSBadgeTone
import com.ts.ds.core.style.TSControlState

private const val SAMPLE_LONG_TITLE = "Automatically download updates over mobile data"
private const val SAMPLE_LONG_SUBTITLE = "Only when Wi-Fi has not been available for more than a day"

@Composable
private fun ListSampleColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TSTheme.colors.backgroundPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) { content() }
}

@Composable
private fun Forced(state: TSControlState?, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTSForcedControlState provides state, content = content)
}

@Composable
internal fun ListItemContentsSample() {
    ListSampleColumn {
        TSListSection("Contents") {
            TSListItem("Title only")
            TSListItem("Inbox", subtitle = "3 unread", onClick = {}, showsChevron = true, leading = { TSListItemIcon(TSIcon.Mail) }) {
                TSBadge("New", tone = TSBadgeTone.Accent)
            }
            TSListItem("Ada Lovelace", subtitle = "Engineer", leading = { TSAvatar("Ada Lovelace") }) {
                TSListItemValue("Admin")
            }
            TSListItem("Wi-Fi", checked = true, onCheckedChange = {}, subtitle = "Home")
        }
        TSList {
            TSListItem(SAMPLE_LONG_TITLE, subtitle = SAMPLE_LONG_SUBTITLE, onClick = {}, showsChevron = true) {
                TSListItemValue("Off")
            }
        }
    }
}

@Composable
internal fun ListItemStatesSample() {
    ListSampleColumn {
        TSList {
            TSListItem("Default", onClick = {}, showsChevron = true)
            Forced(TSControlState.Pressed) { TSListItem("Pressed", onClick = {}, showsChevron = true) }
            Forced(TSControlState.Focused) { TSListItem("Focused", onClick = {}, showsChevron = true) }
            TSListItem("Disabled", subtitle = "Not available", onClick = {}, enabled = false, showsChevron = true, leading = { TSListItemIcon(TSIcon.Lock) })
            Forced(TSControlState.Pressed) { TSListItem("Static pressed", subtitle = "No action, no highlight") }
            TSListItem("Switch off", checked = false, onCheckedChange = {})
            TSListItem("Switch disabled", checked = true, onCheckedChange = {}, enabled = false)
        }
        TSListItem("Standalone row", subtitle = "Outside a list", onClick = {})
    }
}
