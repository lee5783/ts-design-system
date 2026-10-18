package com.ts.ds.compose.components.listitem

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.shared.tsReduceMotion
import com.ts.ds.core.logic.TSListItemAccessibility
import com.ts.ds.core.style.TSListItemLayout
import com.ts.ds.core.style.TSListItemStyleSpec

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
    val expansionState = TSListItemAccessibility.expansionState(LocalContext.current, expanded)
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
                    role = Role.Button,
                    onClick = { onExpandedChange(!expanded) },
                )
                .semantics {
                    stateDescription = expansionState
                    tsListItemDivider = inGroup
                    if (enabled) {
                        if (expanded) {
                            collapse {
                                onExpandedChange(false)
                                true
                            }
                        } else {
                            expand {
                                onExpandedChange(true)
                                true
                            }
                        }
                    }
                },
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
