package com.ts.ds.compose.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.button.TSButton
import com.ts.ds.compose.components.shared.withContentDirectionAtLayoutStart
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.shadow
import com.ts.ds.compose.theme.style
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.logic.TSDialogLayout
import com.ts.ds.core.style.TSDialogActionsAxis
import com.ts.ds.core.style.TSDialogStyleSpec
import com.ts.ds.core.style.TSDialogVariant
import com.ts.ds.core.style.resolve

internal object TSDialogTestTags {
    const val PANEL = "ts.dialog.panel"
    const val SCRIM = "ts.dialog.scrim"
    const val GRABBER = "ts.dialog.grabber"
    const val TITLE = "ts.dialog.title"
    const val ACTIONS = "ts.dialog.actions"
}

@Composable
internal fun resolveDialogStyle(variant: TSDialogVariant): TSDialogStyleSpec.Resolved =
    TSDialogStyleSpec.resolve(TSDialogStyleSpec.configuration(variant, LocalDensity.current.fontScale))

internal fun TSDialogStyleSpec.Resolved.panelShape(): Shape {
    val radius = cornerRadiusDp.dp
    return if (roundsBottomCorners) RoundedCornerShape(radius) else RoundedCornerShape(topStart = radius, topEnd = radius)
}

@Composable
internal fun TSDialogPanel(
    title: String,
    resolved: TSDialogStyleSpec.Resolved,
    actions: List<TSDialogAction>,
    onAction: (TSDialogAction) -> Unit,
    modifier: Modifier = Modifier,
    scrollsContent: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = TSTheme.colors
    val shape = resolved.panelShape()
    val elevation = resolved.shadow?.let { TSTheme.elevation.shadow(it) }
    var chrome = modifier
    resolved.maxWidthDp?.let { chrome = chrome.widthIn(max = it.dp) }
    if (elevation != null) {
        chrome = chrome.shadow(elevation.elevation, shape, clip = false, ambientColor = elevation.color, spotColor = elevation.color)
    }
    Column(
        modifier = chrome
            .fillMaxWidth()
            .background(colors.color(resolved.fill), shape)
            .testTag(TSDialogTestTags.PANEL)
            .padding(
                start = resolved.paddingHorizontalDp.dp,
                end = resolved.paddingHorizontalDp.dp,
                top = resolved.paddingTopDp.dp,
                bottom = resolved.paddingBottomDp.dp,
            ),
    ) {
        if (resolved.showsGrabber) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(resolved.grabberWidthDp.dp, resolved.grabberHeightDp.dp)
                    .background(colors.color(resolved.grabberColor), CircleShape)
                    .testTag(TSDialogTestTags.GRABBER),
            )
            Spacer(Modifier.height(resolved.grabberSpacingDp.dp))
        }
        val body: @Composable () -> Unit = {
            BasicText(
                text = title,
                style = TSTheme.typography.style(resolved.titleTypeRole)
                    .copy(color = colors.color(resolved.titleColor))
                    .withContentDirectionAtLayoutStart(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TSDialogTestTags.TITLE),
            )
            Spacer(Modifier.height(resolved.titleSpacingDp.dp))
            content()
            if (actions.isNotEmpty()) {
                Spacer(Modifier.height(resolved.actionsTopSpacingDp.dp))
                TSDialogActions(resolved, actions, onAction)
            }
        }
        if (scrollsContent) {
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) { body() }
        } else {
            body()
        }
    }
}

@Composable
internal fun TSDialogMessage(message: String, resolved: TSDialogStyleSpec.Resolved) {
    BasicText(
        text = message,
        style = TSTheme.typography.style(resolved.bodyTypeRole)
            .copy(color = TSTheme.colors.color(resolved.bodyColor))
            .withContentDirectionAtLayoutStart(),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun TSDialogActions(
    resolved: TSDialogStyleSpec.Resolved,
    actions: List<TSDialogAction>,
    onAction: (TSDialogAction) -> Unit,
) {
    val ordered = TSDialogLayout.orderedActions(actions, resolved.primaryActionFirst)
    val spacing = resolved.actionsSpacingDp.dp
    if (resolved.actionsAxis == TSDialogActionsAxis.Vertical) {
        Column(
            modifier = Modifier.fillMaxWidth().testTag(TSDialogTestTags.ACTIONS),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            ordered.forEach { action ->
                TSButton(
                    text = action.label,
                    onClick = { onAction(action) },
                    variant = action.variant,
                    modifier = if (resolved.blockActions) Modifier.fillMaxWidth() else Modifier,
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().testTag(TSDialogTestTags.ACTIONS),
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ordered.forEach { action ->
                TSButton(text = action.label, onClick = { onAction(action) }, variant = action.variant)
            }
        }
    }
}
