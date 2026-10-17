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
import com.ts.ds.compose.components.alert.TSAlert
import com.ts.ds.compose.components.button.TSButton
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSAlertTone
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.demo.compose.shell.OptionChip
import com.ts.ds.demo.compose.shell.OptionGroup
import com.ts.ds.demo.compose.shell.ToggleRow
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.sample.SampleData

object AlertPageTags {
    const val TONE_INFO = "demo.alert.tone.info"
    const val PLAYGROUND_TITLE = "demo.alert.playground.title"
    const val PLAYGROUND_DISMISSIBLE = "demo.alert.playground.dismissible"
    const val PLAYGROUND_ACTION = "demo.alert.playground.action"
    const val PLAYGROUND_ALERT = "demo.alert.playground.alert"
    const val PLAYGROUND_SHOW = "demo.alert.playground.show"
    const val PLAYGROUND_RETRY = "demo.alert.playground.retry"
    const val PLAYGROUND_COUNT = "demo.alert.playground.count"

    fun tone(tone: TSAlertTone): String = "demo.alert.playground.tone.${tone.name.lowercase()}"
}

private val TSAlertTone.labelRes: Int
    get() = when (this) {
        TSAlertTone.Info -> DemoR.string.demo_sample_information
        TSAlertTone.Success -> DemoR.string.demo_sample_success
        TSAlertTone.Warning -> DemoR.string.demo_sample_warning
        TSAlertTone.Danger -> DemoR.string.demo_sample_error
    }

fun LazyListScope.alertPage() {
    componentSection("alert.tones", DemoR.string.demo_component_tones) { AlertTones() }
    componentSection("alert.contents", DemoR.string.demo_component_contents) { AlertContents() }
    componentSection("alert.playground", DemoR.string.demo_component_playground) { AlertPlayground() }
}

@Composable
private fun AlertTones() {
    val saved = SampleData.message("saved")
    TSAlert(
        stringResource(DemoR.string.demo_sample_sign_in_body),
        tone = TSAlertTone.Info,
        title = stringResource(DemoR.string.demo_sample_sign_in_title),
        modifier = Modifier.testTag(AlertPageTags.TONE_INFO),
    )
    TSAlert(stringResource(saved.bodyRes), tone = TSAlertTone.Success, title = stringResource(saved.titleRes))
    TSAlert(stringResource(DemoR.string.demo_sample_storage_body), tone = TSAlertTone.Warning, title = stringResource(DemoR.string.demo_sample_storage_title))
    TSAlert(stringResource(DemoR.string.demo_sample_payment_body), tone = TSAlertTone.Danger, title = stringResource(DemoR.string.demo_sample_payment_title))
}

@Composable
private fun AlertContents() {
    val saved = SampleData.message("saved")
    val offline = SampleData.message("offline")
    val deleted = SampleData.message("deleted")
    TSAlert(stringResource(saved.bodyRes))
    TSAlert(stringResource(offline.bodyRes), tone = TSAlertTone.Warning, title = stringResource(offline.titleRes)) {
        TSButton(stringResource(requireNotNull(offline.actionRes)), onClick = {}, variant = TSButtonVariant.Link)
    }
    TSAlert(stringResource(deleted.bodyRes), tone = TSAlertTone.Success, title = stringResource(deleted.titleRes), icon = TSIcon.Trash)
}

@Composable
private fun AlertPlayground() {
    var tone by rememberSaveable { mutableStateOf(TSAlertTone.Info) }
    var showsTitle by rememberSaveable { mutableStateOf(true) }
    var dismissible by rememberSaveable { mutableStateOf(true) }
    var showsAction by rememberSaveable { mutableStateOf(false) }
    var shown by rememberSaveable { mutableStateOf(true) }
    var dismissedOnce by rememberSaveable { mutableStateOf(false) }
    var actions by rememberSaveable { mutableIntStateOf(0) }
    val offline = SampleData.message("offline")

    DemoCaption(text = stringResource(DemoR.string.demo_component_tone))
    OptionGroup {
        TSAlertTone.entries.forEach { option ->
            OptionChip(
                label = stringResource(option.labelRes),
                selected = tone == option,
                onClick = { tone = option },
                modifier = Modifier.testTag(AlertPageTags.tone(option)),
            )
        }
    }
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_title),
        checked = showsTitle,
        onCheckedChange = { showsTitle = it },
        modifier = Modifier.testTag(AlertPageTags.PLAYGROUND_TITLE),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_dismissible),
        checked = dismissible,
        onCheckedChange = { dismissible = it },
        modifier = Modifier.testTag(AlertPageTags.PLAYGROUND_DISMISSIBLE),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_action),
        checked = showsAction,
        onCheckedChange = { showsAction = it },
        modifier = Modifier.testTag(AlertPageTags.PLAYGROUND_ACTION),
    )
    if (shown) {
        val retry = stringResource(requireNotNull(offline.actionRes))
        TSAlert(
            stringResource(offline.bodyRes),
            tone = tone,
            title = if (showsTitle) stringResource(offline.titleRes) else null,
            announcesWhenShown = dismissedOnce,
            onDismiss = if (dismissible) {
                {
                    shown = false
                    dismissedOnce = true
                }
            } else {
                null
            },
            modifier = Modifier.testTag(AlertPageTags.PLAYGROUND_ALERT),
            action = if (showsAction) {
                { TSButton(retry, onClick = { actions++ }, variant = TSButtonVariant.Link, modifier = Modifier.testTag(AlertPageTags.PLAYGROUND_RETRY)) }
            } else {
                null
            },
        )
    }
    TSButton(
        stringResource(DemoR.string.demo_component_show_again),
        onClick = { shown = true },
        variant = TSButtonVariant.Secondary,
        enabled = !shown,
        modifier = Modifier.testTag(AlertPageTags.PLAYGROUND_SHOW),
    )
    DemoTapCounter(count = actions, tag = AlertPageTags.PLAYGROUND_COUNT)
}
