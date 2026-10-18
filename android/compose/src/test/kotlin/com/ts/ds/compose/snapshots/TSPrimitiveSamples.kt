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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.shared.LocalTSForcedControlState
import com.ts.ds.compose.components.spinner.TSSpinner
import com.ts.ds.compose.components.text.TSText
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.style.TSControlState
import com.ts.ds.core.style.TSSpinnerSize
import com.ts.ds.core.style.TSSpinnerTone
import com.ts.ds.core.style.TSTextColor
import com.ts.ds.core.style.TSTypeRole

@Composable
private fun PrimitiveSampleColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TSTheme.colors.backgroundPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) { content() }
}

@Composable
internal fun SpinnerGallerySample() {
    CompositionLocalProvider(LocalTSForcedControlState provides TSControlState.Default) {
        PrimitiveSampleColumn {
            TSSpinnerTone.entries.forEach { tone ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    TSSpinnerSize.entries.forEach { size -> TSSpinner(size = size, tone = tone) }
                    TSText(tone.name, role = TSTypeRole.BodySm, color = TSTextColor.Muted)
                }
            }
        }
    }
}

@Composable
internal fun TextRolesSample() {
    PrimitiveSampleColumn {
        TSTypeRole.entries.forEach { role -> TSText("${role.name} $SAMPLE_TEXT", role = role) }
        TSText("Body, one line, ellipsis at the end of the line", role = TSTypeRole.Body, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun TextColorsSample() {
    val colors = TSTheme.colors
    PrimitiveSampleColumn {
        TSTextColor.entries.forEach { color ->
            val background = when (color) {
                TSTextColor.Inverse -> colors.surfaceInverse
                TSTextColor.OnAccent -> colors.accentPrimary
                else -> Color.Transparent
            }
            TSText(
                text = "${color.name} $SAMPLE_TEXT",
                modifier = Modifier.fillMaxWidth().background(background).padding(4.dp),
                color = color,
            )
        }
    }
}
