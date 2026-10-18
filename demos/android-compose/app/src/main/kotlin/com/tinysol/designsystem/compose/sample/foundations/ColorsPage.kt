package com.tinysol.designsystem.compose.sample.foundations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.theme.TSShapes
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.theme.color
import com.tinysol.designsystem.core.style.TSColorRole
import com.tinysol.designsystem.compose.sample.shell.DemoTokenRow

fun LazyListScope.colorsPage() {
    items(TSColorRole.entries, key = { it.name }) { role ->
        val color = TSTheme.colors.color(role)
        DemoTokenRow(name = role.name, value = "#%08X".format(color.toArgb())) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color, TSShapes.radiusSm)
                    .border(1.dp, TSTheme.colors.borderSubtle, TSShapes.radiusSm),
            )
        }
    }
}
