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
import com.ts.ds.compose.components.button.TSButton
import com.ts.ds.compose.components.dialog.TSDialog
import com.ts.ds.compose.components.toast.LocalTSToastState
import com.ts.ds.compose.components.toast.TSToast
import com.ts.ds.compose.components.toast.TSToastState
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.logic.TSToastTone
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSDialogVariant
import com.ts.ds.demo.compose.shell.OptionChip
import com.ts.ds.demo.compose.shell.OptionGroup
import com.ts.ds.demo.compose.shell.ToggleRow
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.sample.SampleData

object ToastPageTags {
    const val QUEUE_SHOW = "demo.toast.queue.show"
    const val SHEET_OPEN = "demo.toast.sheet.open"
    const val PLAYGROUND_ACTION = "demo.toast.playground.action"
    const val PLAYGROUND_SHOW = "demo.toast.playground.show"
    const val PLAYGROUND_COUNT = "demo.toast.playground.count"

    fun tone(tone: TSToastTone): String = "demo.toast.playground.tone.${tone.name.lowercase()}"
}

private val TSToastTone.labelRes: Int
    get() = when (this) {
        TSToastTone.Neutral -> DemoR.string.demo_sample_neutral
        TSToastTone.Info -> DemoR.string.demo_sample_information
        TSToastTone.Success -> DemoR.string.demo_sample_success
        TSToastTone.Warning -> DemoR.string.demo_sample_warning
        TSToastTone.Danger -> DemoR.string.demo_sample_error
    }

fun LazyListScope.toastPage() {
    componentSection("toast.tones", DemoR.string.demo_component_tones) { ToastTones() }
    componentSection("toast.contents", DemoR.string.demo_component_contents) { ToastContents() }
    componentSection("toast.queue", DemoR.string.demo_component_queue) { ToastQueue() }
    componentSection("toast.sheet", DemoR.string.demo_component_over_sheet) { ToastOverSheet() }
    componentSection("toast.playground", DemoR.string.demo_component_playground) { ToastPlayground() }
}

@Composable
private fun toastState(): TSToastState = requireNotNull(LocalTSToastState.current)

@Composable
private fun ToastTones() {
    val saved = SampleData.message("saved")
    TSToast(stringResource(DemoR.string.demo_sample_project_archived))
    TSToast(stringResource(saved.titleRes), tone = TSToastTone.Success)
    TSToast(stringResource(DemoR.string.demo_sample_storage_title), tone = TSToastTone.Warning)
    TSToast(stringResource(DemoR.string.demo_sample_payment_title), tone = TSToastTone.Danger)
    TSToast(stringResource(DemoR.string.demo_sample_sign_in_title), tone = TSToastTone.Info)
}

@Composable
private fun ToastContents() {
    TSToast(
        stringResource(DemoR.string.demo_sample_project_archived),
        actionLabel = stringResource(DemoR.string.demo_sample_undo),
        onAction = {},
    )
    TSToast(
        stringResource(DemoR.string.demo_sample_long_toast),
        tone = TSToastTone.Info,
        actionLabel = stringResource(DemoR.string.demo_sample_open),
        onAction = {},
    )
}

@Composable
private fun ToastQueue() {
    val state = toastState()
    val messages = listOf(
        stringResource(DemoR.string.demo_sample_photo_uploaded),
        stringResource(DemoR.string.demo_sample_album_shared),
        stringResource(DemoR.string.demo_sample_backup_complete),
    )
    TSButton(
        stringResource(DemoR.string.demo_component_show_three_toasts),
        onClick = { messages.forEach { state.show(it, TSToastTone.Success) } },
        variant = TSButtonVariant.Secondary,
        modifier = Modifier.testTag(ToastPageTags.QUEUE_SHOW),
    )
}

@Composable
private fun ToastOverSheet() {
    val state = toastState()
    var presented by rememberSaveable { mutableStateOf(false) }
    val linkCopied = stringResource(DemoR.string.demo_sample_link_copied)
    TSButton(
        stringResource(DemoR.string.demo_component_open_sheet),
        onClick = { presented = true },
        variant = TSButtonVariant.Secondary,
        modifier = Modifier.testTag(ToastPageTags.SHEET_OPEN),
    )
    TSDialog(
        isPresented = presented,
        onPresentedChange = { presented = it },
        title = stringResource(DemoR.string.demo_sample_toast_sheet_title),
        message = stringResource(DemoR.string.demo_sample_toast_sheet_body),
        variant = TSDialogVariant.Sheet,
        actions = listOf(
            TSDialogAction(stringResource(DemoR.string.demo_sample_close)),
            TSDialogAction(stringResource(DemoR.string.demo_sample_copy_link), TSButtonVariant.Primary, dismisses = false) {
                state.show(linkCopied, TSToastTone.Success)
            },
        ),
    )
}

@Composable
private fun ToastPlayground() {
    val state = toastState()
    var tone by rememberSaveable { mutableStateOf(TSToastTone.Neutral) }
    var showsAction by rememberSaveable { mutableStateOf(true) }
    var undoCount by rememberSaveable { mutableIntStateOf(0) }
    val deleted = SampleData.message("deleted")
    val message = stringResource(deleted.titleRes)
    val undo = stringResource(DemoR.string.demo_sample_undo)

    DemoCaption(text = stringResource(DemoR.string.demo_component_tone))
    OptionGroup {
        TSToastTone.entries.forEach { option ->
            OptionChip(
                label = stringResource(option.labelRes),
                selected = tone == option,
                onClick = { tone = option },
                modifier = Modifier.testTag(ToastPageTags.tone(option)),
            )
        }
    }
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_action),
        checked = showsAction,
        onCheckedChange = { showsAction = it },
        modifier = Modifier.testTag(ToastPageTags.PLAYGROUND_ACTION),
    )
    TSButton(
        stringResource(DemoR.string.demo_component_show_toast),
        onClick = {
            if (showsAction) state.show(message, tone, undo) { undoCount++ } else state.show(message, tone)
        },
        modifier = Modifier.testTag(ToastPageTags.PLAYGROUND_SHOW),
    )
    DemoTapCounter(count = undoCount, tag = ToastPageTags.PLAYGROUND_COUNT)
}
