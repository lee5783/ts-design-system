package com.ts.ds.compose.snapshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.dialog.TSDialogMessage
import com.ts.ds.compose.components.dialog.TSDialogPanel
import com.ts.ds.compose.components.dialog.resolveDialogStyle
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSDialogVariant

private val sampleActions = listOf(
    TSDialogAction("Cancel"),
    TSDialogAction("Delete", TSButtonVariant.Danger),
)

@Composable
private fun DialogSamplePanel(variant: TSDialogVariant) {
    val resolved = resolveDialogStyle(variant)
    TSDialogPanel(
        title = "Delete this project?",
        resolved = resolved,
        actions = sampleActions,
        onAction = {},
        onClose = {},
        scrollsContent = false,
    ) {
        TSDialogMessage("This removes the project and its files for everyone. This cannot be undone.", resolved)
    }
}

@Composable
internal fun DialogCenterSample() {
    val resolved = resolveDialogStyle(TSDialogVariant.Center)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TSTheme.colors.backgroundPrimary)
            .background(TSTheme.colors.color(resolved.scrim))
            .padding(horizontal = resolved.outerMarginDp.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) { DialogSamplePanel(TSDialogVariant.Center) }
}

@Composable
internal fun DialogSheetSample() {
    val resolved = resolveDialogStyle(TSDialogVariant.Sheet)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TSTheme.colors.backgroundPrimary)
            .background(TSTheme.colors.color(resolved.scrim)),
    ) {
        Spacer(Modifier.height(48.dp))
        DialogSamplePanel(TSDialogVariant.Sheet)
    }
}
