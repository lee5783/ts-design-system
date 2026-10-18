package com.tinysol.designsystem.compose.sample.foundations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.theme.TSShapes
import com.tinysol.designsystem.compose.theme.TSSize
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.sample.shell.DemoTokenRow

private val previewLimit: Dp = 64.dp

private val sizeTokens: List<Pair<String, Dp>> = listOf(
    "icon-sm" to TSSize.iconSm,
    "icon-md" to TSSize.iconMd,
    "icon-lg" to TSSize.iconLg,
    "control-sm" to TSSize.controlSm,
    "control-md" to TSSize.controlMd,
    "control-lg" to TSSize.controlLg,
    "nav-bar" to TSSize.navBar,
    "tab-bar" to TSSize.tabBar,
    "content-max" to TSSize.contentMax,
)

fun LazyListScope.sizePage() {
    items(sizeTokens, key = { it.first }) { (name, value) ->
        DemoTokenRow(name = name, value = value.formatDp()) {
            Box(modifier = Modifier.size(previewLimit)) {
                Box(
                    modifier = Modifier
                        .width(value.coerceAtMost(previewLimit))
                        .height(value.coerceAtMost(previewLimit))
                        .background(TSTheme.colors.accentSubtle, TSShapes.radiusXs)
                        .border(1.dp, TSTheme.colors.accentPrimary, TSShapes.radiusXs),
                )
            }
        }
    }
}
