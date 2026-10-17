package com.ts.ds.views.snapshots

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ts.ds.core.logic.TSToastTone
import com.ts.ds.views.components.toast.TSToastView

private val centered: LinearLayout.LayoutParams
    get() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        gravity = Gravity.CENTER_HORIZONTAL
    }

private fun Context.toastColumn(): LinearLayout = galleryColumn().apply {
    dividerDrawable = GradientDrawable().apply { setSize(0, dp(12f)) }
}

private fun Context.toast(message: String, configure: TSToastView.() -> Unit = {}): TSToastView = TSToastView(this).apply {
    this.message = message
    configure()
}

internal fun toastTonesSample(context: Context): View = context.toastColumn().apply {
    addView(context.toast("Project archived"), centered)
    addView(context.toast("Sync resumed") { tone = TSToastTone.Info }, centered)
    addView(context.toast("Changes saved") { tone = TSToastTone.Success }, centered)
    addView(context.toast("Storage almost full") { tone = TSToastTone.Warning }, centered)
    addView(context.toast("Upload failed") { tone = TSToastTone.Danger }, centered)
}

internal fun toastContentsSample(context: Context): View = context.toastColumn().apply {
    addView(
        context.toast("Message deleted") {
            actionLabel = "Undo"
            onAction = {}
        },
        centered,
    )
    addView(
        context.toast("Upload failed") {
            tone = TSToastTone.Danger
            actionLabel = "Retry"
            onAction = {}
        },
        centered,
    )
    addView(
        context.toast("Sync is paused because the device is offline. Changes will upload when the connection is back.") {
            tone = TSToastTone.Warning
        },
        centered,
    )
    addView(
        context.toast("3 conversations moved to the archive. You can find them in Settings.") {
            tone = TSToastTone.Success
            actionLabel = "Undo"
            onAction = {}
        },
        centered,
    )
}
