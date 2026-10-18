package com.tinysol.designsystem.compose.sample.foundations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.theme.TSShapes
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.sample.shell.DemoTokenRow
import com.tinysol.designsystem.tokens.TSTokenValues

private data class RadiusToken(val name: String, val shape: RoundedCornerShape, val value: Float)

private val radiusTokens: List<RadiusToken> = listOf(
    RadiusToken("radius-xs", TSShapes.radiusXs, TSTokenValues.Radius.radiusXs),
    RadiusToken("radius-sm", TSShapes.radiusSm, TSTokenValues.Radius.radiusSm),
    RadiusToken("radius-md", TSShapes.radiusMd, TSTokenValues.Radius.radiusMd),
    RadiusToken("radius-lg", TSShapes.radiusLg, TSTokenValues.Radius.radiusLg),
    RadiusToken("radius-xl", TSShapes.radiusXl, TSTokenValues.Radius.radiusXl),
    RadiusToken("radius-full", TSShapes.radiusFull, TSTokenValues.Radius.radiusFull),
)

fun LazyListScope.radiusPage() {
    items(radiusTokens, key = { it.name }) { token ->
        DemoTokenRow(name = token.name, value = "${token.value.toInt()}dp") {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(TSTheme.colors.accentSubtle, token.shape)
                    .border(2.dp, TSTheme.colors.accentPrimary, token.shape),
            )
        }
    }
}
