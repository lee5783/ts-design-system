package com.ts.ds.compose.components.listitem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.style
import com.ts.ds.compose.theme.withDsFontWeight
import com.ts.ds.core.style.TSListItemStyleSpec

@Composable
public fun TSList(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val group = TSListItemStyleSpec.group
    val colors = TSTheme.colors
    val shape = RoundedCornerShape(group.cornerRadiusDp.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.color(group.fill), shape)
            .border(group.borderWidthDp.dp, colors.color(group.border), shape),
    ) {
        CompositionLocalProvider(LocalTSListGroup provides true) {
            content()
        }
    }
}

@Composable
public fun TSListSection(
    header: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val group = TSListItemStyleSpec.group
    Column(modifier = modifier.fillMaxWidth()) {
        if (!header.isNullOrEmpty()) {
            val style = TSTheme.typography.style(group.headerTypeRole)
            BasicText(
                text = header.uppercase(),
                style = style.withDsFontWeight(group.headerTypeRole.values.fontWeight).copy(color = TSTheme.colors.color(group.headerColor)),
                modifier = Modifier
                    .padding(start = group.headerHorizontalPaddingDp.dp, end = group.headerHorizontalPaddingDp.dp, bottom = group.headerSpacingDp.dp),
            )
        }
        TSList(content = content)
    }
}
