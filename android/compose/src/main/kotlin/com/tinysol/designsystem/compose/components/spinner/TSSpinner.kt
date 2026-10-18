package com.tinysol.designsystem.compose.components.spinner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.components.shared.TSSpinner as TSSpinnerArc
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.theme.color
import com.tinysol.designsystem.core.style.TSSpinnerSize
import com.tinysol.designsystem.core.style.TSSpinnerStyleSpec
import com.tinysol.designsystem.core.style.TSSpinnerTone
import com.tinysol.designsystem.core.style.resolve

@Composable
public fun TSSpinner(
    modifier: Modifier = Modifier,
    size: TSSpinnerSize = TSSpinnerSize.Md,
    tone: TSSpinnerTone = TSSpinnerTone.Primary,
) {
    val resolved = TSSpinnerStyleSpec.resolve(TSSpinnerStyleSpec.Configuration(size, tone))
    TSSpinnerArc(
        color = TSTheme.colors.color(resolved.color),
        size = resolved.sizeDp.dp,
        strokeWidth = resolved.strokeWidthDp.dp,
        rotationMillis = resolved.rotationMillis,
        modifier = modifier,
    )
}
