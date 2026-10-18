package com.tinysol.designsystem.compose.components.toast

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.components.icon.TSIconImage
import com.tinysol.designsystem.compose.components.shared.withContentDirectionAtLayoutStart
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.theme.color
import com.tinysol.designsystem.compose.theme.shadow
import com.tinysol.designsystem.compose.theme.style
import com.tinysol.designsystem.compose.theme.withDsFontWeight
import com.tinysol.designsystem.core.logic.TSToastTone
import com.tinysol.designsystem.core.style.TSToastStyleSpec
import com.tinysol.designsystem.core.style.resolve

internal object TSToastTestTags {
    const val TOAST = "ts.toast"
    const val MESSAGE = "ts.toast.message"
    const val ICON = "ts.toast.icon"
    const val ACTION = "ts.toast.action"
}

@Composable
public fun TSToast(
    message: String,
    modifier: Modifier = Modifier,
    tone: TSToastTone = TSToastTone.Neutral,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val resolved = TSToastStyleSpec.resolve(
        TSToastStyleSpec.Configuration(tone = tone, hasAction = actionLabel != null && onAction != null),
    )
    val colors = TSTheme.colors
    val typography = TSTheme.typography
    val shape = RoundedCornerShape(resolved.cornerRadiusDp.dp)
    val shadow = TSTheme.elevation.shadow(resolved.shadow)
    val spacing = resolved.spacingDp.dp

    Row(
        modifier = modifier
            .testTag(TSToastTestTags.TOAST)
            .widthIn(max = resolved.maxWidthDp.dp)
            .shadow(shadow.elevation, shape, clip = false, ambientColor = shadow.color, spotColor = shadow.color)
            .background(colors.color(resolved.fill), shape)
            .padding(horizontal = resolved.horizontalPaddingDp.dp, vertical = resolved.verticalPaddingDp.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .testTag(TSToastTestTags.MESSAGE),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val icon = resolved.icon
            val iconColor = resolved.iconColor
            if (icon != null && iconColor != null) {
                TSIconImage(
                    icon = icon,
                    contentDescription = null,
                    size = resolved.iconSize.dp.dp,
                    tint = colors.color(iconColor),
                    modifier = Modifier.testTag(TSToastTestTags.ICON),
                )
            }
            BasicText(
                text = message,
                style = typography.style(resolved.messageTypeRole)
                    .copy(color = colors.color(resolved.messageColor))
                    .withContentDirectionAtLayoutStart(),
            )
        }
        if (resolved.showsAction && actionLabel != null && onAction != null) {
            TSToastActionButton(actionLabel, resolved, onAction)
        }
    }
}

@Composable
private fun TSToastActionButton(
    label: String,
    resolved: TSToastStyleSpec.Resolved,
    onAction: () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val style = TSTheme.typography.style(resolved.actionTypeRole)
        .withDsFontWeight(resolved.actionFontWeight)
        .copy(
            color = TSTheme.colors.color(resolved.actionColor),
            textDecoration = if (resolved.actionIsUnderlined) TextDecoration.Underline else null,
        )
    BasicText(
        text = label,
        style = style,
        modifier = Modifier
            .testTag(TSToastTestTags.ACTION)
            .alpha(if (pressed) resolved.actionPressedOpacity else 1f)
            .clickable(interactionSource = source, indication = null, onClick = onAction)
            .padding(start = resolved.actionHorizontalPaddingDp.dp),
    )
}
