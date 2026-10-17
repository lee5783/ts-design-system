package com.ts.ds.compose.components.iconbutton

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.icon.TSIconImage
import com.ts.ds.compose.components.shared.collectTSControlState
import com.ts.ds.compose.components.shared.tsControlChrome
import com.ts.ds.compose.components.shared.tsControlClickable
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSIconButtonStyleSpec
import com.ts.ds.core.style.TSIconButtonVariant

@Composable
public fun TSIconButton(
    icon: TSIcon,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TSIconButtonVariant = TSIconButtonVariant.Ghost,
    round: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val state = source.collectTSControlState(enabled = enabled)
    val resolved = TSIconButtonStyleSpec.resolve(TSIconButtonStyleSpec.Configuration(variant, round), state)

    Box(
        modifier = modifier
            .tsControlClickable(source, state.interactive, onClick, resolved.minTouchTargetDp)
            .semantics { this.contentDescription = contentDescription }
            .tsControlChrome(
                interactionSource = source,
                shape = if (round) CircleShape else RoundedCornerShape(resolved.cornerRadiusDp.dp),
                fill = resolved.fill,
                border = resolved.border,
                borderWidthDp = resolved.borderWidthDp,
                shadow = null,
                opacity = resolved.opacity,
                showsFocusRing = resolved.showsFocusRing,
            )
            .size(resolved.sizeDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        TSIconImage(icon, contentDescription = null, size = resolved.iconSizeDp.dp, tint = TSTheme.colors.color(resolved.icon))
    }
}
