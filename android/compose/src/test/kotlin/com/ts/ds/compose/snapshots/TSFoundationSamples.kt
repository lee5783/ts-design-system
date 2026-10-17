package com.ts.ds.compose.snapshots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.style
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSTypeRole

internal const val SAMPLE_TEXT = "The quick brown fox"

@Composable
internal fun TypographySample() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TSTheme.colors.backgroundPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TSTypeRole.entries.forEach { role ->
            BasicText(
                text = "${role.name} $SAMPLE_TEXT",
                style = TSTheme.typography.style(role).copy(color = TSTheme.colors.textPrimary),
            )
        }
    }
}

@Composable
internal fun ColorSwatchesSample() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TSTheme.colors.backgroundPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TSColorRole.entries.chunked(2).forEach { pair ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { role ->
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            Modifier
                                .size(24.dp)
                                .background(TSTheme.colors.color(role))
                                .border(1.dp, TSTheme.colors.borderStrong),
                        )
                        BasicText(
                            text = role.name,
                            style = TSTheme.typography.caption.copy(color = TSTheme.colors.textSecondary),
                        )
                    }
                }
            }
        }
    }
}
