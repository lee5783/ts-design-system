package com.ts.ds.compose.components.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.icon.TSIconImage
import com.ts.ds.compose.components.icon.TSIconSource
import com.ts.ds.compose.components.shared.TSSpinner
import com.ts.ds.compose.components.shared.collectTSControlState
import com.ts.ds.compose.components.shared.tsControlChrome
import com.ts.ds.compose.components.shared.tsControlClickable
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.style
import com.ts.ds.compose.theme.withDsFontWeight
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSButtonSize
import com.ts.ds.core.style.TSButtonStyleSpec
import com.ts.ds.core.style.TSButtonVariant

@Composable
public fun TSButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TSButtonVariant = TSButtonVariant.Primary,
    size: TSButtonSize = TSButtonSize.Md,
    leadingIcon: TSIcon? = null,
    trailingIcon: TSIcon? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    TSButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        size = size,
        leadingIconSource = leadingIcon?.let(TSIconSource::Ts),
        trailingIconSource = trailingIcon?.let(TSIconSource::Ts),
        enabled = enabled,
        loading = loading,
        maxLines = Int.MAX_VALUE,
        overflow = TextOverflow.Clip,
        interactionSource = interactionSource,
    )
}

@Composable
public fun TSButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TSButtonVariant = TSButtonVariant.Primary,
    size: TSButtonSize = TSButtonSize.Md,
    leadingIconSource: TSIconSource? = null,
    trailingIconSource: TSIconSource? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val state = source.collectTSControlState(enabled = enabled, loading = loading)
    val resolved = TSButtonStyleSpec.resolve(TSButtonStyleSpec.Configuration(variant, size), state)
    val colors = TSTheme.colors
    val contentColor = colors.color(resolved.label)
    val shape = RoundedCornerShape(resolved.cornerRadiusDp.dp)
    val textStyle = TSTheme.typography.style(resolved.typeRole)
        .withDsFontWeight(resolved.fontWeight)
        .copy(
            color = contentColor,
            textAlign = TextAlign.Center,
            textDirection = TextDirection.Content,
            textDecoration = if (resolved.underlined) TextDecoration.Underline else null,
        )

    Row(
        modifier = modifier
            .tsControlClickable(source, state.interactive, onClick, resolved.minTouchTargetDp)
            .tsControlChrome(
                interactionSource = source,
                shape = shape,
                fill = resolved.fill,
                border = resolved.border,
                borderWidthDp = resolved.borderWidthDp,
                shadow = resolved.shadow,
                opacity = resolved.opacity,
                showsFocusRing = resolved.showsFocusRing,
            )
            .defaultMinSize(minHeight = resolved.minHeightDp.dp)
            .padding(horizontal = resolved.horizontalPaddingDp.dp, vertical = resolved.verticalPaddingDp.dp),
        horizontalArrangement = Arrangement.spacedBy(resolved.iconSpacingDp.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (resolved.showsSpinner) {
            Box(Modifier.size(resolved.iconSizeDp.dp), contentAlignment = Alignment.Center) {
                TSSpinner(
                    color = contentColor,
                    size = resolved.spinnerSizeDp.dp,
                    strokeWidth = resolved.spinnerStrokeWidthDp.dp,
                    rotationMillis = resolved.spinnerRotationMillis,
                )
            }
        } else if (leadingIconSource != null) {
            TSIconImage(leadingIconSource, contentDescription = null, size = resolved.iconSizeDp.dp, tint = contentColor)
        }
        BasicText(text = text, style = textStyle, overflow = overflow, maxLines = maxLines)
        if (trailingIconSource != null) {
            TSIconImage(trailingIconSource, contentDescription = null, size = resolved.iconSizeDp.dp, tint = contentColor)
        }
    }
}
