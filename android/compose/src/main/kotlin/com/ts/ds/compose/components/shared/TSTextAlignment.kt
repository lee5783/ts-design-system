package com.ts.ds.compose.components.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
@ReadOnlyComposable
internal fun TextStyle.withContentDirectionAtLayoutStart(): TextStyle = copy(
    textDirection = TextDirection.Content,
    textAlign = if (LocalLayoutDirection.current == LayoutDirection.Rtl) TextAlign.Right else TextAlign.Left,
)
