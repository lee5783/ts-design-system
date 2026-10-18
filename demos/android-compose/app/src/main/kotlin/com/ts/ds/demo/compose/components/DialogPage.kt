package com.ts.ds.demo.compose.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.ts.ds.compose.components.button.TSButton
import com.ts.ds.compose.components.dialog.TSDialog
import com.ts.ds.compose.components.textfield.TSTextField
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.logic.TSDialogPresenter
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSDialogVariant
import com.ts.ds.demo.compose.shell.OptionChip
import com.ts.ds.demo.compose.shell.OptionGroup
import com.ts.ds.demo.compose.shell.ToggleRow
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.sample.SampleData

object DialogPageTags {
    const val OPEN_CENTER = "demo.dialog.open.center"
    const val OPEN_SHEET = "demo.dialog.open.sheet"
    const val RESULT = "demo.dialog.result"
    const val OPEN_INPUT = "demo.dialog.open.input"
    const val INPUT_FIELD = "demo.dialog.input.field"
    const val OPEN_STACKING = "demo.dialog.open.stacking"
    const val REFUSED = "demo.dialog.refused"
    const val QUEUED = "demo.dialog.queued"
    const val OPEN_CHAIN = "demo.dialog.open.chain"
    const val CHAIN_RESULT = "demo.dialog.chain.result"
    const val PLAYGROUND_LONG = "demo.dialog.playground.long"
    const val PLAYGROUND_OPEN = "demo.dialog.playground.open"

    fun variant(variant: TSDialogVariant): String = "demo.dialog.playground.variant.${variant.name.lowercase()}"
}

private val TSDialogVariant.labelRes: Int
    get() = when (this) {
        TSDialogVariant.Center -> DemoR.string.demo_component_center
        TSDialogVariant.Sheet -> DemoR.string.demo_component_sheet
    }

fun LazyListScope.dialogPage() {
    componentSection("dialog.variants", DemoR.string.demo_component_variants) { DialogVariants() }
    componentSection("dialog.contents", DemoR.string.demo_component_contents) { DialogContents() }
    componentSection("dialog.stacking", DemoR.string.demo_component_stacking) { DialogStacking() }
    componentSection("dialog.chaining", DemoR.string.demo_component_chaining) { DialogChaining() }
    componentSection("dialog.playground", DemoR.string.demo_component_playground) { DialogPlayground() }
}

@Composable
private fun rememberDialogResult(): Pair<String, (String) -> Unit> {
    val none = stringResource(DemoR.string.demo_component_none)
    var result by rememberSaveable { mutableStateOf(none) }
    return result to { result = it }
}

@Composable
private fun deleteActions(onResult: (String) -> Unit): List<TSDialogAction> {
    val cancel = stringResource(DemoR.string.demo_sample_cancel)
    val delete = stringResource(DemoR.string.demo_sample_delete)
    return listOf(
        TSDialogAction(cancel) { onResult(cancel) },
        TSDialogAction(delete, TSButtonVariant.Danger) { onResult(delete) },
    )
}

@Composable
private fun DialogVariants() {
    var centerPresented by rememberSaveable { mutableStateOf(false) }
    var sheetPresented by rememberSaveable { mutableStateOf(false) }
    val (result, setResult) = rememberDialogResult()
    val confirm = SampleData.message("confirm-delete")
    val dismissed = stringResource(DemoR.string.demo_component_dismissed)
    DemoComponentRow {
        TSButton(
            stringResource(DemoR.string.demo_component_open_dialog),
            onClick = { centerPresented = true },
            variant = TSButtonVariant.Secondary,
            modifier = Modifier.testTag(DialogPageTags.OPEN_CENTER),
        )
        TSButton(
            stringResource(DemoR.string.demo_component_open_sheet),
            onClick = { sheetPresented = true },
            variant = TSButtonVariant.Secondary,
            modifier = Modifier.testTag(DialogPageTags.OPEN_SHEET),
        )
    }
    DemoCaption(
        text = stringResource(DemoR.string.demo_component_dialog_result, result),
        modifier = Modifier.testTag(DialogPageTags.RESULT),
    )
    TSDialog(
        isPresented = centerPresented,
        onPresentedChange = { centerPresented = it },
        title = stringResource(confirm.titleRes),
        message = stringResource(confirm.bodyRes),
        actions = deleteActions(setResult),
        onDismiss = { setResult(dismissed) },
    )
    TSDialog(
        isPresented = sheetPresented,
        onPresentedChange = { sheetPresented = it },
        title = stringResource(confirm.titleRes),
        message = stringResource(confirm.bodyRes),
        variant = TSDialogVariant.Sheet,
        actions = deleteActions(setResult),
        onDismiss = { setResult(dismissed) },
    )
}

@Composable
private fun DialogContents() {
    var presented by rememberSaveable { mutableStateOf(false) }
    val initialName = stringResource(DemoR.string.demo_sample_project_name_value)
    var projectName by rememberSaveable { mutableStateOf(initialName) }
    val (result, setResult) = rememberDialogResult()
    val cancel = stringResource(DemoR.string.demo_sample_cancel)
    TSButton(
        stringResource(DemoR.string.demo_component_with_input),
        onClick = { presented = true },
        variant = TSButtonVariant.Secondary,
        modifier = Modifier.testTag(DialogPageTags.OPEN_INPUT),
    )
    DemoCaption(text = stringResource(DemoR.string.demo_component_dialog_result, result))
    val dismissed = stringResource(DemoR.string.demo_component_dismissed)
    TSDialog(
        isPresented = presented,
        onPresentedChange = { presented = it },
        title = stringResource(DemoR.string.demo_sample_rename_title),
        actions = listOf(
            TSDialogAction(cancel) { setResult(cancel) },
            TSDialogAction(stringResource(DemoR.string.demo_sample_rename), TSButtonVariant.Primary) { setResult(projectName) },
        ),
        onDismiss = { setResult(dismissed) },
    ) {
        TSTextField(
            value = projectName,
            label = stringResource(DemoR.string.demo_sample_project_name),
            onValueChange = { projectName = it },
            modifier = Modifier.testTag(DialogPageTags.INPUT_FIELD),
        )
    }
}

@Composable
private fun DialogStacking() {
    var stackingPresented by rememberSaveable { mutableStateOf(false) }
    var secondPresented by rememberSaveable { mutableStateOf(false) }
    val presenter = TSDialogPresenter.shared
    var refused by remember { mutableIntStateOf(presenter.refusedCount) }
    var queued by remember { mutableIntStateOf(presenter.pendingCount) }
    DisposableEffect(presenter) {
        val listener: (TSDialogPresenter) -> Unit = {
            refused = it.refusedCount
            queued = it.pendingCount
        }
        presenter.addListener(listener)
        onDispose { presenter.removeListener(listener) }
    }
    val confirm = SampleData.message("confirm-delete")
    TSButton(
        stringResource(DemoR.string.demo_component_stacking),
        onClick = { stackingPresented = true },
        variant = TSButtonVariant.Secondary,
        modifier = Modifier.testTag(DialogPageTags.OPEN_STACKING),
    )
    DemoCaption(
        text = stringResource(DemoR.string.demo_component_queued_count, queued),
        modifier = Modifier.testTag(DialogPageTags.QUEUED),
    )
    DemoCaption(
        text = stringResource(DemoR.string.demo_component_refused_count, refused),
        modifier = Modifier.testTag(DialogPageTags.REFUSED),
    )
    TSDialog(
        isPresented = stackingPresented,
        onPresentedChange = { stackingPresented = it },
        title = stringResource(DemoR.string.demo_sample_stacking_title),
        message = stringResource(DemoR.string.demo_sample_stacking_queue_body),
        actions = listOf(
            TSDialogAction(stringResource(DemoR.string.demo_sample_close)),
            TSDialogAction(stringResource(DemoR.string.demo_component_open_another), TSButtonVariant.Primary, dismisses = false) {
                secondPresented = true
            },
        ),
    )
    TSDialog(
        isPresented = secondPresented,
        onPresentedChange = { secondPresented = it },
        title = stringResource(confirm.titleRes),
        message = stringResource(confirm.bodyRes),
        actions = deleteActions {},
    )
}

@Composable
private fun DialogChaining() {
    var deletePresented by rememberSaveable { mutableStateOf(false) }
    var deletedPresented by rememberSaveable { mutableStateOf(false) }
    val (result, setResult) = rememberDialogResult()
    val confirm = SampleData.message("confirm-delete")
    val cancel = stringResource(DemoR.string.demo_sample_cancel)
    val close = stringResource(DemoR.string.demo_sample_close)
    val undo = stringResource(DemoR.string.demo_sample_undo)
    TSButton(
        stringResource(DemoR.string.demo_component_delete_with_undo),
        onClick = { deletePresented = true },
        variant = TSButtonVariant.Secondary,
        modifier = Modifier.testTag(DialogPageTags.OPEN_CHAIN),
    )
    DemoCaption(
        text = stringResource(DemoR.string.demo_component_dialog_result, result),
        modifier = Modifier.testTag(DialogPageTags.CHAIN_RESULT),
    )
    TSDialog(
        isPresented = deletePresented,
        onPresentedChange = { deletePresented = it },
        title = stringResource(confirm.titleRes),
        message = stringResource(confirm.bodyRes),
        actions = listOf(
            TSDialogAction(cancel) { setResult(cancel) },
            TSDialogAction(stringResource(DemoR.string.demo_sample_delete), TSButtonVariant.Danger) { deletedPresented = true },
        ),
    )
    TSDialog(
        isPresented = deletedPresented,
        onPresentedChange = { deletedPresented = it },
        title = stringResource(DemoR.string.demo_sample_project_deleted_title),
        message = stringResource(DemoR.string.demo_sample_project_deleted_body),
        actions = listOf(
            TSDialogAction(close) { setResult(close) },
            TSDialogAction(undo, TSButtonVariant.Primary) { setResult(undo) },
        ),
    )
}

@Composable
private fun DialogPlayground() {
    var variant by rememberSaveable { mutableStateOf(TSDialogVariant.Center) }
    var longContent by rememberSaveable { mutableStateOf(false) }
    var presented by rememberSaveable { mutableStateOf(false) }
    val (result, setResult) = rememberDialogResult()
    val confirm = SampleData.message("confirm-delete")
    val cancel = stringResource(DemoR.string.demo_sample_cancel)
    val confirmLabel = stringResource(if (longContent) DemoR.string.demo_sample_archive else DemoR.string.demo_sample_delete)
    val dismissed = stringResource(DemoR.string.demo_component_dismissed)

    DemoCaption(text = stringResource(DemoR.string.demo_component_variant))
    OptionGroup {
        TSDialogVariant.entries.forEach { option ->
            OptionChip(
                label = stringResource(option.labelRes),
                selected = variant == option,
                onClick = { variant = option },
                modifier = Modifier.testTag(DialogPageTags.variant(option)),
            )
        }
    }
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_long_content),
        checked = longContent,
        onCheckedChange = { longContent = it },
        modifier = Modifier.testTag(DialogPageTags.PLAYGROUND_LONG),
    )
    TSButton(
        stringResource(DemoR.string.demo_component_open_dialog),
        onClick = { presented = true },
        modifier = Modifier.testTag(DialogPageTags.PLAYGROUND_OPEN),
    )
    DemoCaption(text = stringResource(DemoR.string.demo_component_dialog_result, result))
    TSDialog(
        isPresented = presented,
        onPresentedChange = { presented = it },
        title = stringResource(if (longContent) DemoR.string.demo_sample_archive_title else confirm.titleRes),
        message = stringResource(if (longContent) DemoR.string.demo_sample_long_dialog_body else confirm.bodyRes),
        variant = variant,
        actions = listOf(
            TSDialogAction(cancel) { setResult(cancel) },
            TSDialogAction(confirmLabel, if (longContent) TSButtonVariant.Primary else TSButtonVariant.Danger) { setResult(confirmLabel) },
        ),
        onDismiss = { setResult(dismissed) },
    )
}
