package com.tinysol.designsystem.compose.components.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.style.TSIconSize
import com.tinysol.designsystem.core.style.TSIconStyleSpec
import com.tinysol.designsystem.core.style.resolve

@Composable
public fun TSIcon(
    icon: TSIcon,
    modifier: Modifier = Modifier,
    size: TSIconSize = TSIconSize.Md,
    tint: Color = TSTheme.colors.textPrimary,
) {
    val resolved = TSIconStyleSpec.resolve(TSIconStyleSpec.Configuration(size))
    TSIconImage(
        icon = icon,
        contentDescription = null,
        size = resolved.sizeDp.dp,
        tint = tint,
        modifier = modifier,
    )
}

@Composable
internal fun TSIconImage(
    icon: TSIcon,
    contentDescription: String?,
    size: Dp,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    TSIconImage(TSIconSource.Ts(icon), contentDescription, size, tint, modifier)
}

@Composable
internal fun TSIconImage(
    source: TSIconSource,
    contentDescription: String?,
    size: Dp,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = source.painter(),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(tint),
    )
}
