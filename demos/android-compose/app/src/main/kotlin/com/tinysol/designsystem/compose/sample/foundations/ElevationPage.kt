package com.tinysol.designsystem.compose.sample.foundations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.theme.TSElevation
import com.tinysol.designsystem.compose.theme.TSShadow
import com.tinysol.designsystem.compose.theme.TSShapes
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.sample.shell.DemoTokenRow

private val shadowTokens: List<Pair<String, (TSElevation) -> TSShadow>> = listOf(
    "shadow-xs" to TSElevation::shadowXs,
    "shadow-sm" to TSElevation::shadowSm,
    "shadow-md" to TSElevation::shadowMd,
    "shadow-lg" to TSElevation::shadowLg,
)

fun LazyListScope.elevationPage() {
    items(shadowTokens, key = { it.first }) { (name, select) ->
        val shadow = select(TSTheme.elevation)
        DemoTokenRow(name = name, value = shadow.elevation.formatDp()) {
            ShadowPreview(shadow)
        }
    }
}

@Composable
private fun ShadowPreview(shadow: TSShadow) {
    Box(modifier = Modifier.padding(12.dp)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(shadow.elevation, TSShapes.radiusMd, ambientColor = shadow.color, spotColor = shadow.color)
                .background(TSTheme.colors.surfaceElevated, TSShapes.radiusMd),
        )
    }
}
