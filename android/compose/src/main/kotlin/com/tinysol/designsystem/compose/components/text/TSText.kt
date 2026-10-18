package com.tinysol.designsystem.compose.components.text

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.tinysol.designsystem.compose.components.shared.withContentDirectionAtLayoutStart
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.theme.color
import com.tinysol.designsystem.compose.theme.style
import com.tinysol.designsystem.core.style.TSTextColor
import com.tinysol.designsystem.core.style.TSTypeRole

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
