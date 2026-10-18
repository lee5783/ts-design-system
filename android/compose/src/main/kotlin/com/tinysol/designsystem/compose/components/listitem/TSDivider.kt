package com.tinysol.designsystem.compose.components.listitem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.theme.color
import com.tinysol.designsystem.core.style.TSListItemStyleSpec

@Composable
public fun TSDivider(modifier: Modifier = Modifier) {
    val group = TSListItemStyleSpec.group
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = group.dividerInsetDp.dp)
            .height(group.dividerThicknessDp.dp)
            .background(TSTheme.colors.color(group.divider)),
    )
}
