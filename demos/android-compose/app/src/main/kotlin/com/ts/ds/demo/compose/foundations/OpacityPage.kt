package com.ts.ds.demo.compose.foundations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.theme.TSShapes
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.demo.compose.shell.DemoTokenRow
import com.ts.ds.tokens.TSTokenValues

private val opacityTokens: List<Pair<String, Float>> = listOf(
    "opacity-full" to 1f,
    "opacity-pressed" to TSTokenValues.Opacity.opacityPressed,
    "opacity-disabled" to TSTokenValues.Opacity.opacityDisabled,
)

fun LazyListScope.opacityPage() {
    items(opacityTokens, key = { it.first }) { (name, value) ->
        DemoTokenRow(name = name, value = "${(value * 100).toInt()}%") {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .alpha(value)
                    .background(TSTheme.colors.accentPrimary, TSShapes.radiusSm),
            )
        }
    }
}
