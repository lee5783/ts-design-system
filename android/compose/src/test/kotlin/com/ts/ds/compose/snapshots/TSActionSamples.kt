package com.ts.ds.compose.snapshots

import androidx.compose.foundation.background
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
