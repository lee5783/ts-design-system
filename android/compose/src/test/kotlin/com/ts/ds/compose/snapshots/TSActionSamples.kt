package com.ts.ds.compose.snapshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import com.ts.ds.compose.components.icon.TSIconSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.button.TSButton
import com.ts.ds.compose.components.icon.TSIcon
import com.ts.ds.compose.components.iconbutton.TSIconButton
import com.ts.ds.compose.components.shared.LocalTSForcedControlState
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSButtonSize
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSControlState
import com.ts.ds.core.style.TSIconButtonVariant
import com.ts.ds.core.style.TSIconSize
import com.ts.ds.compose.theme.color

internal val SampleControllerIcon: ImageVector = sampleControllerIcon(24)

internal fun sampleControllerIcon(sizeDp: Int): ImageVector = ImageVector.Builder(
    name = "SampleController",
    defaultWidth = sizeDp.dp,
    defaultHeight = sizeDp.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.75f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(7f, 7f)
        horizontalLineTo(17f)
        arcTo(5f, 5f, 0f, false, true, 17f, 17f)
        horizontalLineTo(7f)
        arcTo(5f, 5f, 0f, false, true, 7f, 7f)
        close()
        moveTo(7f, 10f)
        verticalLineTo(14f)
        moveTo(5f, 12f)
        horizontalLineTo(9f)
    }
    path(fill = SolidColor(Color.Red)) {
        moveTo(16f, 10f)
        arcTo(1f, 1f, 0f, true, true, 16f, 12f)
        arcTo(1f, 1f, 0f, true, true, 16f, 10f)
        close()
    }
    path(fill = SolidColor(Color.Blue)) {
        moveTo(18f, 12f)
        arcTo(1f, 1f, 0f, true, true, 18f, 14f)
        arcTo(1f, 1f, 0f, true, true, 18f, 12f)
        close()
    }
}.build()

internal enum class SampleState { Default, Pressed, Disabled, Loading, Focused }

@Composable
private fun Forced(state: SampleState, content: @Composable () -> Unit) {
    val forced = when (state) {
        SampleState.Pressed -> TSControlState.Pressed
        SampleState.Focused -> TSControlState.Focused
        SampleState.Loading -> TSControlState.Loading
        SampleState.Default -> TSControlState.Default
        SampleState.Disabled -> TSControlState.Disabled
    }
    CompositionLocalProvider(LocalTSForcedControlState provides forced, content = content)
}

@Composable
internal fun SampleColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TSTheme.colors.backgroundPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}

@Composable
internal fun ButtonGallerySample(variant: TSButtonVariant) {
    SampleColumn {
        TSButtonSize.entries.forEach { size ->
            SampleState.entries.forEach { state ->
                Forced(state) {
                    TSButton(
                        text = "${size.name} ${state.name}",
                        onClick = {},
                        variant = variant,
                        size = size,
                        enabled = state != SampleState.Disabled,
                        loading = state == SampleState.Loading,
                    )
                }
            }
        }
        TSButton(text = "Leading", onClick = {}, variant = variant, leadingIcon = TSIcon.ArrowLeft)
        TSButton(text = "Trailing", onClick = {}, variant = variant, trailingIcon = TSIcon.ArrowRight)
        TSButton(text = "Block", onClick = {}, variant = variant, leadingIcon = TSIcon.Plus, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
internal fun ButtonCustomIconSample(variant: TSButtonVariant) {
    val largePainter = rememberVectorPainter(sampleControllerIcon(48))
    SampleColumn {
        TSButtonSize.entries.forEach { size ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TSButton(text = size.name, onClick = {}, variant = variant, size = size, leadingIcon = TSIcon.Plus)
                TSButton(
                    text = size.name,
                    onClick = {},
                    variant = variant,
                    size = size,
                    leadingIconSource = TSIconSource.Vector(SampleControllerIcon),
                )
            }
        }
        listOf(SampleState.Pressed, SampleState.Disabled, SampleState.Loading).forEach { state ->
            Forced(state) {
                TSButton(
                    text = state.name,
                    onClick = {},
                    variant = variant,
                    leadingIconSource = TSIconSource.Vector(SampleControllerIcon),
                    trailingIconSource = TSIconSource.Vector(SampleControllerIcon),
                    enabled = state != SampleState.Disabled,
                    loading = state == SampleState.Loading,
                )
            }
        }
        TSButton(text = "Painter", onClick = {}, variant = variant, leadingIconSource = TSIconSource.Painter(largePainter))
        TSButton(
            text = "Join the session with a very long label",
            onClick = {},
            variant = variant,
            leadingIconSource = TSIconSource.Vector(SampleControllerIcon),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(220.dp),
        )
        TSButton(
            text = "Join the session with a very long label",
            onClick = {},
            variant = variant,
            leadingIconSource = TSIconSource.Vector(SampleControllerIcon),
            modifier = Modifier.width(220.dp),
        )
    }
}

@Composable
internal fun IconButtonCustomIconSample() {
    SampleColumn {
        TSIconButtonVariant.entries.forEach { variant ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TSIconButton(icon = TSIcon.Plus, contentDescription = "TS", onClick = {}, variant = variant)
                SampleState.entries.filter { it != SampleState.Loading }.forEach { state ->
                    Forced(state) {
                        TSIconButton(
                            icon = TSIconSource.Vector(SampleControllerIcon),
                            contentDescription = state.name,
                            onClick = {},
                            variant = variant,
                            round = state == SampleState.Focused,
                            enabled = state != SampleState.Disabled,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun IconButtonGallerySample() {
    SampleColumn {
        TSIconButtonVariant.entries.forEach { variant ->
            listOf(false, true).forEach { round ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SampleState.entries.filter { it != SampleState.Loading }.forEach { state ->
                        Forced(state) {
                            TSIconButton(
                                icon = if (round) TSIcon.Plus else TSIcon.ChevronRight,
                                contentDescription = state.name,
                                onClick = {},
                                variant = variant,
                                round = round,
                                enabled = state != SampleState.Disabled,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun IconGallerySample() {
    SampleColumn {
        TSIconSize.entries.forEach { size ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(TSIcon.Home, TSIcon.ChevronLeft, TSIcon.ArrowRight, TSIcon.Logout, TSIcon.Search).forEach {
                    TSIcon(it, contentDescription = null, size = size)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                TSIcon.CheckCircle to TSColorRole.Success,
                TSIcon.Alert to TSColorRole.Warning,
                TSIcon.XCircle to TSColorRole.Danger,
                TSIcon.Info to TSColorRole.Info,
            ).forEach { (icon, role) ->
                TSIcon(icon, contentDescription = role.name, size = TSIconSize.Lg, tint = TSTheme.colors.color(role))
            }
        }
    }
}
