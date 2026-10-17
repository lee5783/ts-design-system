package com.ts.ds.demo.compose.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.ts.ds.compose.components.badge.TSBadge
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSBadgeTone
import com.ts.ds.demo.compose.shell.OptionChip
import com.ts.ds.demo.compose.shell.OptionGroup
import com.ts.ds.demo.compose.shell.ToggleRow
import com.ts.ds.demo.support.R as DemoR

object BadgePageTags {
    const val PLAYGROUND_BADGE = "demo.badge.playground.badge"
    const val PLAYGROUND_DOT = "demo.badge.playground.dot"
    const val PLAYGROUND_ICON = "demo.badge.playground.icon"
}

fun LazyListScope.badgePage() {
    componentSection("badge.tones", DemoR.string.demo_component_tones) {
        TSBadgeTone.entries.forEach { tone ->
            DemoComponentRow {
                TSBadge(stringResource(badgeSampleText(tone)), tone = tone)
                DemoCaption(text = tone.name.lowercase())
            }
        }
    }
    componentSection("badge.with-dot", DemoR.string.demo_component_with_dot) {
        DemoComponentRow {
            TSBadgeTone.entries.forEach { tone -> TSBadge(stringResource(badgeSampleText(tone)), tone = tone, showsDot = true) }
        }
    }
    componentSection("badge.with-icons", DemoR.string.demo_component_with_icons) {
        DemoComponentRow {
            TSBadgeTone.entries.forEach { tone -> TSBadge(stringResource(badgeSampleText(tone)), tone = tone, icon = badgeSampleIcon(tone)) }
        }
    }
    componentSection("badge.truncation", DemoR.string.demo_component_truncation) {
        TSBadge(stringResource(DemoR.string.demo_sample_needs_review), tone = TSBadgeTone.Warning, showsDot = true)
    }
    componentSection("badge.playground", DemoR.string.demo_component_playground) { BadgePlayground() }
}

@Composable
private fun BadgePlayground() {
    var tone by rememberSaveable { mutableStateOf(TSBadgeTone.Neutral) }
    var showsDot by rememberSaveable { mutableStateOf(false) }
    var showsIcon by rememberSaveable { mutableStateOf(false) }

    DemoCaption(text = stringResource(DemoR.string.demo_component_tone))
    OptionGroup {
        TSBadgeTone.entries.forEach { option ->
            OptionChip(label = option.name.lowercase(), selected = tone == option, onClick = { tone = option })
        }
    }
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_dot),
        checked = showsDot,
        onCheckedChange = { showsDot = it },
        modifier = Modifier.testTag(BadgePageTags.PLAYGROUND_DOT),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_icon),
        checked = showsIcon,
        onCheckedChange = { showsIcon = it },
        modifier = Modifier.testTag(BadgePageTags.PLAYGROUND_ICON),
    )
    TSBadge(
        text = stringResource(badgeSampleText(tone)),
        tone = tone,
        showsDot = showsDot,
        icon = if (showsIcon) badgeSampleIcon(tone) else null,
        modifier = Modifier.testTag(BadgePageTags.PLAYGROUND_BADGE),
    )
}

private fun badgeSampleText(tone: TSBadgeTone): Int = when (tone) {
    TSBadgeTone.Neutral -> DemoR.string.demo_sample_draft
    TSBadgeTone.Accent -> DemoR.string.demo_sample_new
    TSBadgeTone.Success -> DemoR.string.demo_sample_active
    TSBadgeTone.Warning -> DemoR.string.demo_sample_pending
    TSBadgeTone.Danger -> DemoR.string.demo_sample_failed
    TSBadgeTone.Info -> DemoR.string.demo_sample_beta
}

private fun badgeSampleIcon(tone: TSBadgeTone): TSIcon = when (tone) {
    TSBadgeTone.Neutral -> TSIcon.Edit
    TSBadgeTone.Accent -> TSIcon.Star
    TSBadgeTone.Success -> TSIcon.CheckCircle
    TSBadgeTone.Warning -> TSIcon.Alert
    TSBadgeTone.Danger -> TSIcon.XCircle
    TSBadgeTone.Info -> TSIcon.Info
}
