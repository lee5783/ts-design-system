package com.tinysol.designsystem.compose.components.iconbutton

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.components.icon.TSIconImage
import com.tinysol.designsystem.compose.components.icon.TSIconSource
import com.tinysol.designsystem.compose.components.shared.collectTSControlState
import com.tinysol.designsystem.compose.components.shared.tsControlChrome
import com.tinysol.designsystem.compose.components.shared.tsControlClickable
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.theme.color
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.style.TSIconButtonStyleSpec
import com.tinysol.designsystem.core.style.TSIconButtonVariant

@Composable
public fun TSIconButton(
    icon: TSIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TSIconButtonVariant = TSIconButtonVariant.Ghost,
    round: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    TSIconButton(
        icon = TSIconSource.Ts(icon),
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        round = round,
        enabled = enabled,
        interactionSource = interactionSource,
    )
}

@Composable
public fun TSIconButton(
    icon: TSIconSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TSIconButtonVariant = TSIconButtonVariant.Ghost,
    round: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val state = source.collectTSControlState(enabled = enabled)
    val resolved = TSIconButtonStyleSpec.resolve(TSIconButtonStyleSpec.Configuration(variant, round), state)

    Box(
        modifier = modifier
            .tsControlClickable(source, state.interactive, onClick, resolved.minTouchTargetDp)
            .tsControlChrome(
                interactionSource = source,
                shape = if (round) CircleShape else RoundedCornerShape(resolved.cornerRadiusDp.dp),
                fill = resolved.fill,
                border = resolved.border,
                borderWidthDp = resolved.borderWidthDp,
                shadow = null,
                opacity = resolved.opacity,
                showsFocusRing = resolved.showsFocusRing,
            )
            .size(resolved.sizeDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        TSIconImage(icon, contentDescription = null, size = resolved.iconSizeDp.dp, tint = TSTheme.colors.color(resolved.icon))
    }
}
