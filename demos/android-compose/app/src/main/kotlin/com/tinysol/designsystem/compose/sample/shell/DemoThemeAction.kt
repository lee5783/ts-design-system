package com.tinysol.designsystem.compose.sample.shell

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.foundation.tsFocusRing
import com.tinysol.designsystem.compose.foundation.tsMinimumTouchTarget
import com.tinysol.designsystem.compose.theme.TSShapes
import com.tinysol.designsystem.compose.theme.TSSize
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.settings.DemoTheme

@Immutable
data class DemoThemeControl(
    val theme: DemoTheme,
    val onCycle: () -> Unit,
)

val LocalDemoThemeControl = staticCompositionLocalOf<DemoThemeControl?> { null }

@Composable
fun DemoThemeAction(
    control: DemoThemeControl,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val description = stringResource(DemoR.string.demo_action_theme, stringResource(control.theme.labelRes))
    val tint = TSTheme.colors.textPrimary
    Box(
        modifier = modifier
            .testTag(DemoTags.ACTION_THEME)
            .tsMinimumTouchTarget()
            .tsFocusRing(interactionSource, TSShapes.radiusFull, TSTheme.colors.focus)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = control.onCycle,
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(TSSize.iconMd)) { drawThemeMark(control.theme, tint) }
    }
}

private fun DrawScope.drawThemeMark(theme: DemoTheme, tint: Color) {
    val stroke = 2.dp.toPx()
    val radius = (size.minDimension - stroke) / 2f * 0.85f
    val topLeft = Offset(center.x - radius, center.y - radius)
    val diameter = Size(radius * 2f, radius * 2f)
    when (theme) {
        DemoTheme.System -> drawArc(tint, startAngle = 90f, sweepAngle = 180f, useCenter = true, topLeft = topLeft, size = diameter)
        DemoTheme.Dark -> drawCircle(tint, radius, center)
        DemoTheme.Light -> Unit
    }
    drawCircle(tint, radius, center, style = Stroke(stroke))
}
