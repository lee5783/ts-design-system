package com.ts.ds.compose.components.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.icon.TSIconImage
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.style
import com.ts.ds.compose.theme.withDsFontWeight
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSBadgeStyleSpec
import com.ts.ds.core.style.TSBadgeTone
import com.ts.ds.core.style.resolve

@Composable
public fun TSBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: TSBadgeTone = TSBadgeTone.Neutral,
    showsDot: Boolean = false,
    icon: TSIcon? = null,
) {
    val resolved = TSBadgeStyleSpec.resolve(TSBadgeStyleSpec.Configuration(tone, showsDot, icon != null))
    val colors = TSTheme.colors
    val foreground = colors.color(resolved.foreground)
    val shape = RoundedCornerShape(resolved.cornerRadiusDp.dp)
    val textStyle = TSTheme.typography.style(resolved.typeRole)
        .withDsFontWeight(resolved.fontWeight)
        .copy(color = foreground, textDirection = TextDirection.Content)
    val border = resolved.border

    Row(
        modifier = modifier
            .clip(shape)
            .background(colors.color(resolved.fill), shape)
            .then(if (border != null) Modifier.border(resolved.borderWidthDp.dp, colors.color(border), shape) else Modifier)
            .defaultMinSize(minHeight = resolved.minHeightDp.dp)
            .padding(horizontal = resolved.horizontalPaddingDp.dp, vertical = resolved.verticalPaddingDp.dp),
        horizontalArrangement = Arrangement.spacedBy(resolved.spacingDp.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (resolved.showsDot) {
            Box(Modifier.size(resolved.dotSizeDp.dp).background(foreground, CircleShape))
        }
        if (icon != null && resolved.showsIcon) {
            TSIconImage(icon, contentDescription = null, size = resolved.iconSizeDp.dp, tint = foreground)
        }
        BasicText(
            text = text,
            modifier = Modifier.weight(1f, fill = false),
            style = textStyle,
            maxLines = resolved.maxLines,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
