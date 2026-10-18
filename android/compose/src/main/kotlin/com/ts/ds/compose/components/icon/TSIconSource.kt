package com.ts.ds.compose.components.icon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import com.ts.ds.core.icon.TSIcon
import androidx.compose.ui.graphics.painter.Painter as ComposePainter

@Stable
public sealed interface TSIconSource {
    @Immutable
    public data class Ts(val icon: TSIcon) : TSIconSource

    @Immutable
    public data class Vector(val imageVector: ImageVector) : TSIconSource

    @Stable
    public data class Painter(val painter: ComposePainter) : TSIconSource
}

@Composable
internal fun TSIconSource.painter(): ComposePainter = when (this) {
    is TSIconSource.Ts -> painterResource(icon.drawableRes)
    is TSIconSource.Vector -> rememberVectorPainter(imageVector)
    is TSIconSource.Painter -> painter
}
