package com.ts.ds.compose.snapshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.alert.TSAlert
import com.ts.ds.compose.components.button.TSButton
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSAlertTone
import com.ts.ds.core.style.TSButtonVariant

@Composable
private fun AlertSampleColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TSTheme.colors.backgroundPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) { content() }
}

@Composable
internal fun AlertTonesSample() {
    AlertSampleColumn {
        TSAlert("Your profile is visible to your team.", tone = TSAlertTone.Info, title = "Heads up")
        TSAlert("Your changes were saved.", tone = TSAlertTone.Success, title = "Saved")
        TSAlert("Free up space to keep syncing.", tone = TSAlertTone.Warning, title = "Storage almost full")
        TSAlert("We could not upload 2 files.", tone = TSAlertTone.Danger, title = "Upload failed")
    }
}

@Composable
internal fun AlertContentsSample() {
    AlertSampleColumn {
        TSAlert("A message without a title.")
        TSAlert("You can dismiss this alert.", tone = TSAlertTone.Success, title = "Dismissible", onDismiss = {})
        TSAlert("We could not upload 2 files.", tone = TSAlertTone.Danger, title = "Upload failed", onDismiss = {}) {
            TSButton("Retry", onClick = {}, variant = TSButtonVariant.Ghost)
        }
        TSAlert("A custom icon replaces the tone icon.", title = "New feature", icon = TSIcon.Star)
        TSAlert(
            "Sync is paused because the device is offline. Changes will upload automatically when the connection is back.",
            tone = TSAlertTone.Warning,
            title = "Offline for longer than a day",
            onDismiss = {},
        )
    }
}
