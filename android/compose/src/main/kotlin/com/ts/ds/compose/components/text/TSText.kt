package com.ts.ds.compose.components.text

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.ts.ds.compose.components.shared.withContentDirectionAtLayoutStart
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.style
import com.ts.ds.core.style.TSTextColor
import com.ts.ds.core.style.TSTypeRole

@Composable
public fun TSText(
    text: String,
    modifier: Modifier = Modifier,
    role: TSTypeRole = TSTypeRole.Body,
    color: TSTextColor = TSTextColor.Primary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val style = TSTheme.typography.style(role)
        .withContentDirectionAtLayoutStart()
        .let { if (textAlign != null) it.copy(textAlign = textAlign) else it }
        .copy(color = TSTheme.colors.color(color.role))
    BasicText(
        text = if (role == TSTypeRole.Overline) text.uppercase() else text,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
    )
}
