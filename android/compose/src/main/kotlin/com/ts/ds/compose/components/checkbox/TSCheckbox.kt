package com.ts.ds.compose.components.checkbox

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.icon.TSIconImage
import com.ts.ds.compose.components.shared.TSToggleRow
import com.ts.ds.compose.components.shared.collectTSControlState
import com.ts.ds.compose.components.shared.tsControlChrome
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.core.style.TSCheckboxStyleSpec

@Composable
public fun TSCheckbox(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    labelHidden: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val state = source.collectTSControlState(enabled = enabled)
    val resolved = TSCheckboxStyleSpec.resolve(TSCheckboxStyleSpec.Configuration(checked), state)
    val shape = RoundedCornerShape(resolved.markCornerRadiusDp.dp)

    TSToggleRow(
        checked = checked,
        label = label,
        onCheckedChange = onCheckedChange,
        labelHidden = labelHidden,
        enabled = state.interactive,
        labelTypeRole = resolved.labelTypeRole,
        labelColor = resolved.labelColor,
        spacingDp = resolved.spacingDp,
        minTouchTargetDp = resolved.minTouchTargetDp,
        opacity = resolved.opacity,
        interactionSource = source,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .tsControlChrome(
                    interactionSource = source,
                    shape = shape,
                    fill = resolved.markFill,
                    border = resolved.markBorder,
                    borderWidthDp = resolved.markBorderWidthDp,
                    shadow = null,
                    opacity = 1f,
                    showsFocusRing = resolved.showsFocusRing,
                )
                .size(resolved.markSizeDp.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (resolved.showsCheck) {
                TSIconImage(
                    icon = resolved.checkIcon,
                    contentDescription = null,
                    size = resolved.checkSizeDp.dp,
                    tint = TSTheme.colors.color(resolved.checkColor),
                )
            }
        }
    }
}
