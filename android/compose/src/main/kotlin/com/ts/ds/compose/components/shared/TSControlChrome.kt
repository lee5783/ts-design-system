package com.ts.ds.compose.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.foundation.tsFocusRing
import com.ts.ds.compose.foundation.tsVisibleFocusRing
import com.ts.ds.compose.foundation.tsMinimumTouchTarget
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.shadow
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSControlState
import com.ts.ds.core.style.TSShadowRole

@Suppress("ComposeCompositionLocalUsage")
internal val LocalTSDimmedByContainer = staticCompositionLocalOf { false }

@Composable
internal fun MutableInteractionSource.collectTSControlState(enabled: Boolean, loading: Boolean = false): TSControlState {
    val pressed by collectIsPressedAsState()
    val forced = LocalTSForcedControlState.current
    return TSControlState.make(
        pressed = forced?.pressed ?: pressed,
        enabled = enabled,
        loading = loading,
        focused = forced?.focused ?: false,
    )
}

internal fun Modifier.tsControlClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    onClick: () -> Unit,
    minTouchTargetDp: Float,
): Modifier = clickable(
    interactionSource = interactionSource,
    indication = null,
    enabled = enabled,
    role = Role.Button,
    onClick = onClick,
).tsMinimumTouchTarget(minTouchTargetDp.dp)

@Composable
internal fun Modifier.tsControlChrome(
    interactionSource: MutableInteractionSource,
    shape: Shape,
    fill: TSColorRole?,
    border: TSColorRole?,
    borderWidthDp: Float,
    shadow: TSShadowRole?,
    opacity: Float,
    showsFocusRing: Boolean,
): Modifier {
    val colors = TSTheme.colors
    val elevation = shadow?.let { TSTheme.elevation.shadow(it) }
    val ring = if (showsFocusRing) tsVisibleFocusRing(shape, colors.focus) else tsFocusRing(interactionSource, shape, colors.focus)
    var chrome = ring.alpha(if (LocalTSDimmedByContainer.current) 1f else opacity)
    if (elevation != null) {
        chrome = chrome.shadow(elevation.elevation, shape, clip = false, ambientColor = elevation.color, spotColor = elevation.color)
    }
    chrome = chrome.background(fill?.let(colors::color) ?: Color.Transparent, shape)
    if (border != null && borderWidthDp > 0f) {
        chrome = chrome.border(borderWidthDp.dp, colors.color(border), shape)
    }
    return chrome
}
