package com.tinysol.designsystem.compose.sample.foundations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.tinysol.designsystem.compose.theme.TSSize
import com.tinysol.designsystem.compose.theme.TSSpacing
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.sample.shell.DemoTokenRow

private val spacingTokens: List<Pair<String, Dp>> = listOf(
    "space-0.5" to TSSpacing.space0_5,
    "space-1" to TSSpacing.space1,
    "space-2" to TSSpacing.space2,
    "space-3" to TSSpacing.space3,
    "space-4" to TSSpacing.space4,
    "space-5" to TSSpacing.space5,
    "space-6" to TSSpacing.space6,
    "space-8" to TSSpacing.space8,
    "space-10" to TSSpacing.space10,
    "space-12" to TSSpacing.space12,
    "space-16" to TSSpacing.space16,
)

fun LazyListScope.spacingPage() {
    items(spacingTokens, key = { it.first }) { (name, value) ->
        DemoTokenRow(name = name, value = value.formatDp()) {
            Box(
                modifier = Modifier
                    .width(value)
                    .height(TSSize.iconMd)
                    .background(TSTheme.colors.accentPrimary),
            )
        }
    }
}

internal fun Dp.formatDp(): String =
    if (value % 1f == 0f) "${value.toInt()}dp" else "${value}dp"
