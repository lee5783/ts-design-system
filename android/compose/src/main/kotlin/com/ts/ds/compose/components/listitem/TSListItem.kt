package com.ts.ds.compose.components.listitem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.icon.TSIconImage
import com.ts.ds.compose.components.shared.TSListItemDividerKey
import com.ts.ds.compose.components.shared.collectTSControlState
import com.ts.ds.compose.components.switchcontrol.TSSwitchTrack
import com.ts.ds.compose.foundation.rememberTSHaptics
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.style
import com.ts.ds.core.R as CoreR
import com.ts.ds.core.haptics.DsHaptics
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.logic.TSListItemAccessibility
import com.ts.ds.core.logic.TSListItemRole
import com.ts.ds.core.style.TSControlState
import com.ts.ds.core.style.TSListItemLayout
import com.ts.ds.core.style.TSListItemStyleSpec
import com.ts.ds.core.style.resolve

internal object TSListItemTestTags {
    const val CHEVRON = "ts.list-item.chevron"
    const val SWITCH = "ts.list-item.switch"
    const val LEADING = "ts.list-item.leading"
}

@Suppress("ComposeCompositionLocalUsage")
internal val LocalTSListGroup = staticCompositionLocalOf { false }

private var SemanticsPropertyReceiver.tsListItemDivider by TSListItemDividerKey

@Composable
public fun TSListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    showsChevron: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val role = TSListItemRole.make(hasAction = onClick != null, showsChevron = showsChevron, hasToggle = false)
    val state = source.collectTSListItemState(enabled)
    val resolved = TSListItemStyleSpec.resolve(
        TSListItemStyleSpec.Configuration(
            hasSubtitle = !subtitle.isNullOrEmpty(),
            isInteractive = role.isPressable,
            layout = TSListItemLayout.make(LocalDensity.current.fontScale),
        ),
        state,
    )
    val hint = TSListItemAccessibility.hint(LocalContext.current, role)
    val inGroup = LocalTSListGroup.current
    val interaction = if (onClick != null) {
        Modifier.clickable(
            interactionSource = source,
            indication = null,
            enabled = state.interactive,
            onClickLabel = hint,
            role = Role.Button,
            onClick = onClick,
        )
    } else {
        Modifier.semantics(mergeDescendants = true) { if (!enabled) disabled() }
    }

    TSListItemRow(
        resolved = resolved,
        inGroup = inGroup,
        modifier = modifier.then(interaction).semantics { tsListItemDivider = inGroup },
        title = title,
        subtitle = subtitle,
        showsChevron = showsChevron,
        leading = leading,
        trailing = trailing,
    )
}

@Composable
public fun TSListItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val switchState = source.collectTSControlState(enabled = enabled)
    val resolved = TSListItemStyleSpec.resolve(
        TSListItemStyleSpec.Configuration(
            hasSubtitle = !subtitle.isNullOrEmpty(),
            isInteractive = false,
            layout = TSListItemLayout.make(LocalDensity.current.fontScale),
        ),
        TSControlState.make(enabled = enabled),
    )
    val haptics = rememberTSHaptics()
    val toggle: (Boolean) -> Unit = { value ->
        haptics.perform(DsHaptics.toggleEvent(value))
        onCheckedChange(value)
    }
    val label = TSListItemAccessibility.label(title, subtitle)
    val stateDescription = stringResource(if (checked) CoreR.string.ts_switch_on else CoreR.string.ts_switch_off)
    val inGroup = LocalTSListGroup.current

    TSListItemRow(
        resolved = resolved,
        inGroup = inGroup,
        modifier = modifier.clearAndSetSemantics {
            contentDescription = label
            role = Role.Switch
            toggleableState = ToggleableState(checked)
            this.stateDescription = stateDescription
            tsListItemDivider = inGroup
            if (enabled) {
                onClick {
                    toggle(!checked)
                    true
                }
            } else {
                disabled()
            }
        },
        title = title,
        subtitle = subtitle,
        showsChevron = false,
        leading = leading,
        trailing = {
            Box(
                modifier = Modifier
                    .testTag(TSListItemTestTags.SWITCH)
                    .toggleable(
                        value = checked,
                        interactionSource = source,
                        indication = null,
                        enabled = enabled,
                        role = Role.Switch,
                        onValueChange = toggle,
                    ),
            ) {
                TSSwitchTrack(checked = checked, state = switchState, interactionSource = source)
            }
        },
    )
}

@Composable
public fun TSListItemIcon(
    icon: TSIcon,
    modifier: Modifier = Modifier,
) {
    val tile = TSListItemStyleSpec.tile
    val colors = TSTheme.colors
    val shape = RoundedCornerShape(tile.cornerRadiusDp.dp)
    Box(
        modifier = modifier
            .size(tile.sizeDp.dp)
            .background(colors.color(tile.fill), shape),
        contentAlignment = Alignment.Center,
    ) {
        TSIconImage(icon, contentDescription = null, size = tile.iconSize.dp.dp, tint = colors.color(tile.foreground))
    }
}

@Composable
public fun TSListItemValue(
    text: String,
    modifier: Modifier = Modifier,
) {
    val resolved = TSListItemStyleSpec.resolve(TSListItemStyleSpec.Configuration())
    BasicText(
        text = text,
        modifier = modifier,
        style = TSTheme.typography.style(resolved.trailingTypeRole).copy(color = TSTheme.colors.color(resolved.trailingColor), textDirection = TextDirection.Content),
    )
}

@Composable
private fun MutableInteractionSource.collectTSListItemState(enabled: Boolean): TSControlState {
    val base = collectTSControlState(enabled = enabled)
    val focused by collectIsFocusedAsState()
    return TSControlState.make(pressed = base.pressed, enabled = enabled, focused = base.focused || focused)
}

@Composable
private fun TSListItemRow(
    resolved: TSListItemStyleSpec.Resolved,
    inGroup: Boolean,
    title: String,
    subtitle: String?,
    showsChevron: Boolean,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = TSTheme.colors
    val dividerColor = colors.color(TSListItemStyleSpec.group.divider)
    val dividerThickness = TSListItemStyleSpec.group.dividerThicknessDp.dp
    val spacing = resolved.spacingDp.dp
    val chevronGap = if (trailing != null && !resolved.isStacked) resolved.trailingSpacingDp.dp else spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                if (inGroup) {
                    drawRect(dividerColor, Offset.Zero, Size(size.width, dividerThickness.toPx()))
                }
            }
            .background(resolved.background?.let(colors::color) ?: Color.Transparent)
            .heightIn(min = resolved.minHeightDp.dp)
            .padding(horizontal = resolved.horizontalPaddingDp.dp, vertical = resolved.verticalPaddingDp.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f).alpha(resolved.opacity)) {
            if (resolved.isStacked) {
                Column(verticalArrangement = Arrangement.spacedBy(resolved.stackedSpacingDp.dp)) {
                    leading?.let { TSListItemLeading(it) }
                    TSListItemText(resolved, title, subtitle)
                    trailing?.let { it() }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing), verticalAlignment = Alignment.CenterVertically) {
                    leading?.let { TSListItemLeading(it) }
                    Box(Modifier.weight(1f)) { TSListItemText(resolved, title, subtitle) }
                    trailing?.let { it() }
                }
            }
        }
        if (showsChevron) {
            Spacer(Modifier.width(chevronGap))
            TSIconImage(
                icon = resolved.chevron,
                contentDescription = null,
                size = resolved.chevronSize.dp.dp,
                tint = colors.color(resolved.chevronColor),
                modifier = Modifier.testTag(TSListItemTestTags.CHEVRON).alpha(resolved.opacity),
            )
        }
    }
}

@Composable
private fun TSListItemLeading(content: @Composable () -> Unit) {
    Box(Modifier.testTag(TSListItemTestTags.LEADING).clearAndSetSemantics {}) { content() }
}

@Composable
private fun TSListItemText(resolved: TSListItemStyleSpec.Resolved, title: String, subtitle: String?) {
    val colors = TSTheme.colors
    val typography = TSTheme.typography
    Column {
        BasicText(
            text = title,
            style = typography.style(resolved.titleTypeRole).copy(color = colors.color(resolved.titleColor), textDirection = TextDirection.Content),
            maxLines = resolved.titleMaxLines ?: Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis,
        )
        if (resolved.showsSubtitle && subtitle != null) {
            BasicText(
                text = subtitle,
                style = typography.style(resolved.subtitleTypeRole).copy(color = colors.color(resolved.subtitleColor), textDirection = TextDirection.Content),
                maxLines = resolved.subtitleMaxLines ?: Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
