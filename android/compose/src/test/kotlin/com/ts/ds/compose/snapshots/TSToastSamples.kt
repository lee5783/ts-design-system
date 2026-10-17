package com.ts.ds.compose.snapshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.toast.TSToast
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.logic.TSToastTone

@Composable
private fun ToastSampleColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TSTheme.colors.backgroundPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}

@Composable
internal fun ToastTonesSample() {
    ToastSampleColumn {
        TSToast("Project archived")
        TSToast("Sync resumed", tone = TSToastTone.Info)
        TSToast("Changes saved", tone = TSToastTone.Success)
        TSToast("Storage almost full", tone = TSToastTone.Warning)
        TSToast("Upload failed", tone = TSToastTone.Danger)
    }
}

@Composable
internal fun ToastContentsSample() {
    ToastSampleColumn {
        TSToast("Message deleted", actionLabel = "Undo", onAction = {})
        TSToast("Upload failed", tone = TSToastTone.Danger, actionLabel = "Retry", onAction = {})
        TSToast("Sync is paused because the device is offline. Changes will upload when the connection is back.", tone = TSToastTone.Warning)
        TSToast(
            "3 conversations moved to the archive. You can find them in Settings.",
            tone = TSToastTone.Success,
            actionLabel = "Undo",
            onAction = {},
        )
    }
}
