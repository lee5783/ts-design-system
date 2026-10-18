package com.tinysol.designsystem.compose.components.listitem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.components.shared.tsReduceMotion
import com.tinysol.designsystem.core.style.TSListItemLayout
import com.tinysol.designsystem.core.style.TSListItemStyleSpec

@Composable
public fun TSDisclosureListItem(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    leading: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val state = source.collectTSListItemState(enabled)
    val resolved = TSListItemStyleSpec.resolve(
        TSListItemStyleSpec.Configuration(
            hasSubtitle = !subtitle.isNullOrEmpty(),
            isInteractive = true,
            layout = TSListItemLayout.make(LocalDensity.current.fontScale),
        ),
        state,
    )
    val disclosure = TSListItemStyleSpec.disclosure
    val rightToLeft = LocalLayoutDirection.current == LayoutDirection.Rtl
    val curve = remember(disclosure.animationEasing) {
        disclosure.animationEasing.let { CubicBezierEasing(it.x1, it.y1, it.x2, it.y2) }
    }
    val rotation by animateFloatAsState(
        targetValue = disclosure.rotationDegrees(expanded, rightToLeft),
        animationSpec = tween(disclosure.animationDurationMillis(tsReduceMotion()), easing = curve),
        label = "ts.list-item.disclosure",
    )
    val inGroup = LocalTSListGroup.current

    Column(modifier = modifier.fillMaxWidth()) {
        TSListItemRow(
            resolved = resolved,
            inGroup = inGroup,
            modifier = Modifier
                .clickable(
                    interactionSource = source,
                    indication = null,
                    enabled = state.interactive,
                    onClick = { onExpandedChange(!expanded) },
                )
                .semantics { tsListItemDivider = inGroup },
            title = title,
            subtitle = subtitle,
            showsChevron = true,
            leading = leading,
            trailing = null,
            chevronRotation = rotation,
        )
        if (expanded) {
            Box(
                modifier = Modifier
                    .testTag(TSListItemTestTags.DISCLOSURE_BODY)
                    .fillMaxWidth()
                    .padding(
                        start = disclosure.bodyHorizontalPaddingDp.dp,
                        end = disclosure.bodyHorizontalPaddingDp.dp,
                        bottom = disclosure.bodyBottomPaddingDp.dp,
                    )
                    .alpha(resolved.opacity),
            ) {
                content()
            }
        }
    }
}
