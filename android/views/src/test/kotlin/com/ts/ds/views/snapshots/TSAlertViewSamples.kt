package com.ts.ds.views.snapshots

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSAlertTone
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.views.components.alert.TSAlertView
import com.ts.ds.views.components.button.TSButtonView

private val fillWidth: LinearLayout.LayoutParams
    get() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

private fun Context.alertColumn(): LinearLayout = galleryColumn().apply {
    dividerDrawable = GradientDrawable().apply { setSize(0, dp(12f)) }
}

private fun Context.alert(message: String, configure: TSAlertView.() -> Unit = {}): TSAlertView = TSAlertView(this).apply {
    this.message = message
    configure()
}

internal fun alertTonesSample(context: Context): View = context.alertColumn().apply {
    addView(context.alert("Your profile is visible to your team.") { title = "Heads up" }, fillWidth)
    addView(
        context.alert("Your changes were saved.") {
            tone = TSAlertTone.Success
            title = "Saved"
        },
        fillWidth,
    )
    addView(
        context.alert("Free up space to keep syncing.") {
            tone = TSAlertTone.Warning
            title = "Storage almost full"
        },
        fillWidth,
    )
    addView(
        context.alert("We could not upload 2 files.") {
            tone = TSAlertTone.Danger
            title = "Upload failed"
        },
        fillWidth,
    )
}

internal fun alertContentsSample(context: Context): View = context.alertColumn().apply {
    addView(context.alert("A message without a title."), fillWidth)
    addView(
        context.alert("You can dismiss this alert.") {
            tone = TSAlertTone.Success
            title = "Dismissible"
            onDismiss = {}
        },
        fillWidth,
    )
    addView(
        context.alert("We could not upload 2 files.") {
            tone = TSAlertTone.Danger
            title = "Upload failed"
            onDismiss = {}
            actionView = TSButtonView(context).apply {
                text = "Retry"
                variant = TSButtonVariant.Ghost
            }
        },
        fillWidth,
    )
    addView(
        context.alert("A custom icon replaces the tone icon.") {
            title = "New feature"
            icon = TSIcon.Star
        },
        fillWidth,
    )
    addView(
        context.alert("Sync is paused because the device is offline. Changes will upload automatically when the connection is back.") {
            tone = TSAlertTone.Warning
            title = "Offline for longer than a day"
            onDismiss = {}
        },
        fillWidth,
    )
}
