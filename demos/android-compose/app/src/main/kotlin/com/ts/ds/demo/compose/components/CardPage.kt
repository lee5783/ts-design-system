package com.ts.ds.demo.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.badge.TSBadge
import com.ts.ds.compose.components.card.TSCard
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.logic.TSCardAccessory
import com.ts.ds.core.logic.TSCardAction
import com.ts.ds.core.style.TSBadgeTone
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSCardStyleSpec
import com.ts.ds.demo.compose.shell.ToggleRow
import com.ts.ds.demo.support.R as DemoR

object CardPageTags {
    const val STATE_DEFAULT = "demo.card.state.default"
    const val STATE_DISABLED = "demo.card.state.disabled"
    const val STATE_COUNT = "demo.card.state.count"
    const val STATE_LAST_ACTION = "demo.card.state.last-action"
    const val PLAYGROUND_SUBTITLE = "demo.card.playground.subtitle"
    const val PLAYGROUND_RAISED = "demo.card.playground.raised"
    const val PLAYGROUND_INTERACTIVE = "demo.card.playground.interactive"
    const val PLAYGROUND_FOOTER = "demo.card.playground.footer"
    const val PLAYGROUND_DISABLED = "demo.card.playground.disabled"
    const val PLAYGROUND_CARD = "demo.card.playground.card"
    const val PLAYGROUND_COUNT = "demo.card.playground.count"
}

fun LazyListScope.cardPage() {
    componentSection("card.variants", DemoR.string.demo_component_variants) { CardVariants() }
    componentSection("card.states", DemoR.string.demo_component_states) { CardStates() }
    componentSection("card.playground", DemoR.string.demo_component_playground) { CardPlayground() }
}

@Composable
private fun CardStack(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(TSCardStyleSpec.STACK_SPACING_DP.dp)) { content() }
}

@Composable
private fun CardVariants() {
    val reportBody = stringResource(DemoR.string.demo_sample_report_body)
    CardStack {
        TSCard(
            text = reportBody,
            title = stringResource(DemoR.string.demo_sample_weekly_report),
            subtitle = stringResource(DemoR.string.demo_sample_updated_today),
            accessory = { TSBadge(stringResource(DemoR.string.demo_sample_new), tone = TSBadgeTone.Accent) },
        )
        TSCard(
            text = reportBody,
            title = stringResource(DemoR.string.demo_component_raised),
            raised = true,
            accessory = TSCardAccessory.IconButton(TSIcon.More, stringResource(DemoR.string.demo_sample_more_options)) {},
        )
        TSCard(
            text = stringResource(DemoR.string.demo_sample_invite_body),
            title = stringResource(DemoR.string.demo_sample_invite_team),
            footerActions = listOf(
                TSCardAction(stringResource(DemoR.string.demo_sample_cancel)) {},
                TSCardAction(stringResource(DemoR.string.demo_sample_send_invite), TSButtonVariant.Primary) {},
            ),
        )
    }
}

@Composable
private fun CardStates() {
    var taps by rememberSaveable { mutableIntStateOf(0) }
    val none = stringResource(DemoR.string.demo_component_none)
    var lastAction by rememberSaveable { mutableStateOf<String?>(null) }
    val share = stringResource(DemoR.string.demo_sample_share)
    val open = stringResource(DemoR.string.demo_sample_open)
    val reportBody = stringResource(DemoR.string.demo_sample_report_body)
    val interactive = stringResource(DemoR.string.demo_component_interactive)
    CardStack {
        TSCard(
            text = reportBody,
            title = stringResource(DemoR.string.demo_component_default),
            subtitle = interactive,
            footerActions = listOf(
                TSCardAction(share) { lastAction = share },
                TSCardAction(open, TSButtonVariant.Primary) { lastAction = open },
            ),
            onClick = { taps++ },
            modifier = Modifier.testTag(CardPageTags.STATE_DEFAULT),
        )
        TSCard(
            text = reportBody,
            title = stringResource(DemoR.string.demo_component_disabled),
            subtitle = interactive,
            onClick = { taps++ },
            enabled = false,
            modifier = Modifier.testTag(CardPageTags.STATE_DISABLED),
        )
    }
    DemoTapCounter(count = taps, tag = CardPageTags.STATE_COUNT)
    DemoCaption(
        text = stringResource(DemoR.string.demo_component_last_action, lastAction ?: none),
        modifier = Modifier.testTag(CardPageTags.STATE_LAST_ACTION),
    )
}

@Composable
private fun CardPlayground() {
    var showsSubtitle by rememberSaveable { mutableStateOf(true) }
    var raised by rememberSaveable { mutableStateOf(false) }
    var interactive by rememberSaveable { mutableStateOf(true) }
    var showsFooter by rememberSaveable { mutableStateOf(false) }
    var disabled by rememberSaveable { mutableStateOf(false) }
    var taps by rememberSaveable { mutableIntStateOf(0) }

    ToggleRow(
        label = stringResource(DemoR.string.demo_component_subtitle),
        checked = showsSubtitle,
        onCheckedChange = { showsSubtitle = it },
        modifier = Modifier.testTag(CardPageTags.PLAYGROUND_SUBTITLE),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_raised),
        checked = raised,
        onCheckedChange = { raised = it },
        modifier = Modifier.testTag(CardPageTags.PLAYGROUND_RAISED),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_interactive),
        checked = interactive,
        onCheckedChange = { interactive = it },
        modifier = Modifier.testTag(CardPageTags.PLAYGROUND_INTERACTIVE),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_footer),
        checked = showsFooter,
        onCheckedChange = { showsFooter = it },
        modifier = Modifier.testTag(CardPageTags.PLAYGROUND_FOOTER),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_disabled),
        checked = disabled,
        onCheckedChange = { disabled = it },
        modifier = Modifier.testTag(CardPageTags.PLAYGROUND_DISABLED),
    )
    val open = stringResource(DemoR.string.demo_sample_open)
    TSCard(
        text = stringResource(DemoR.string.demo_sample_report_body),
        title = stringResource(DemoR.string.demo_sample_weekly_report),
        subtitle = if (showsSubtitle) stringResource(DemoR.string.demo_sample_updated_today) else null,
        raised = raised,
        footerActions = if (showsFooter) listOf(TSCardAction(open, TSButtonVariant.Primary) { taps++ }) else emptyList(),
        onClick = if (interactive) ({ taps++ }) else null,
        enabled = !disabled,
        modifier = Modifier.testTag(CardPageTags.PLAYGROUND_CARD),
    )
    DemoTapCounter(count = taps, tag = CardPageTags.PLAYGROUND_COUNT)
}
