package com.ts.ds.views.snapshots

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSDialogVariant
import com.ts.ds.views.components.dialog.TSDialogPanelView
import com.ts.ds.views.theme.tsColor

private fun Context.dialogPanel(variant: TSDialogVariant): TSDialogPanelView = TSDialogPanelView(this).apply {
    this.variant = variant
    title = "Delete this project?"
    message = "This removes the project and its files for everyone. This cannot be undone."
    actions = listOf(TSDialogAction("Cancel"), TSDialogAction("Delete", TSButtonVariant.Danger))
}

private fun Context.scrimBackground(): LayerDrawable = LayerDrawable(
    arrayOf(ColorDrawable(tsColor(TSColorRole.BackgroundPrimary)), ColorDrawable(tsColor(TSColorRole.Overlay))),
)

internal fun dialogCenterSample(context: Context): View = FrameLayout(context).apply {
    background = context.scrimBackground()
    val margin = context.dp(16f)
    setPadding(margin, context.dp(48f), margin, context.dp(48f))
    addView(
        context.dialogPanel(TSDialogVariant.Center),
        FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER),
    )
}

internal fun dialogSheetSample(context: Context): View = FrameLayout(context).apply {
    background = context.scrimBackground()
    setPadding(0, context.dp(48f), 0, 0)
    addView(
        context.dialogPanel(TSDialogVariant.Sheet),
        FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM),
    )
}
