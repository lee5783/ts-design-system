package com.tinysol.designsystem.compose.components.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.components.icon.TSIconImage
import com.tinysol.designsystem.compose.components.shared.withContentDirectionAtLayoutStart
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.theme.color
import com.tinysol.designsystem.compose.theme.style
import com.tinysol.designsystem.compose.theme.withDsFontWeight
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.style.TSAlertStyleSpec
import com.tinysol.designsystem.core.style.TSAlertTone
import com.tinysol.designsystem.core.style.resolve

internal object TSAlertTestTags {
    const val MESSAGE = "ts.alert.message"
    const val ICON = "ts.alert.icon"
    const val DISMISS = "ts.alert.dismiss"
}

@Composable
public fun TSAlert(
    message: String,
    modifier: Modifier = Modifier,
    tone: TSAlertTone = TSAlertTone.Info,
    title: String? = null,
    icon: TSIcon? = null,
    onDismiss: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val resolved = TSAlertStyleSpec.resolve(
        TSAlertStyleSpec.Configuration(tone = tone, hasTitle = !title.isNullOrEmpty(), isDismissible = onDismiss != null),
    )
    val colors = TSTheme.colors
    val typography = TSTheme.typography
    val spacing = resolved.spacingDp.dp

    Row(
        modifier = modifier
            .background(colors.color(resolved.fill), RoundedCornerShape(resolved.cornerRadiusDp.dp))
            .padding(horizontal = resolved.contentHorizontalInsetDp.dp, vertical = resolved.contentVerticalInsetDp.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.Top,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .testTag(TSAlertTestTags.MESSAGE),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.Top,
        ) {
            TSIconImage(
                icon = icon ?: resolved.icon,
                contentDescription = null,
                size = resolved.iconSize.dp.dp,
                tint = colors.color(resolved.iconColor),
                modifier = Modifier.testTag(TSAlertTestTags.ICON),
            )
            Column {
                if (resolved.showsTitle && title != null) {
                    BasicText(
                        text = title,
                        style = typography.style(resolved.titleTypeRole)
                            .withDsFontWeight(resolved.titleFontWeight)
                            .copy(color = colors.color(resolved.titleColor)).withContentDirectionAtLayoutStart(),
                    )
                }
                BasicText(
                    text = message,
                    style = typography.style(resolved.messageTypeRole).copy(color = colors.color(resolved.messageColor)).withContentDirectionAtLayoutStart(),
                )
            }
        }
        if (action != null) {
            Box { action() }
        }
        if (onDismiss != null && resolved.showsDismiss) {
            TSAlertDismissButton(resolved, onDismiss)
        }
    }
}

@Composable
private fun TSAlertDismissButton(
    resolved: TSAlertStyleSpec.Resolved,
    onDismiss: () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    TSIconImage(
        icon = resolved.dismissIcon,
        contentDescription = null,
        size = resolved.dismissIconSize.dp.dp,
        tint = TSTheme.colors.color(resolved.dismissColor),
        modifier = Modifier
            .testTag(TSAlertTestTags.DISMISS)
            .alpha(if (pressed) resolved.dismissPressedOpacity else 1f)
            .clickable(interactionSource = source, indication = null, onClick = onDismiss)
    )
}
