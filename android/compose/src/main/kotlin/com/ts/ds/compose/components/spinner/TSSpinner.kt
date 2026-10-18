package com.ts.ds.compose.components.spinner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.shared.TSSpinner as TSSpinnerArc
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.core.style.TSSpinnerSize
import com.ts.ds.core.style.TSSpinnerStyleSpec
import com.ts.ds.core.style.TSSpinnerTone
import com.ts.ds.core.style.resolve

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
