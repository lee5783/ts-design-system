package com.ts.ds.compose.components.card

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.button.TSButton
import com.ts.ds.compose.components.iconbutton.TSIconButton
import com.ts.ds.compose.components.shared.LocalTSDimmedByContainer
import com.ts.ds.compose.components.shared.collectTSControlState
import com.ts.ds.compose.components.shared.tsControlChrome
import com.ts.ds.compose.components.shared.withContentDirectionAtLayoutStart
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.style
import com.ts.ds.core.logic.TSCardAccessibility
import com.ts.ds.core.logic.TSCardAccessory
import com.ts.ds.core.logic.TSCardAction
import com.ts.ds.core.style.TSCardStyleSpec
import com.ts.ds.core.style.TSControlState
import com.ts.ds.core.style.resolve

internal object TSCardTestTags {
    const val CONTENT = "ts.card.content"
    const val ACCESSORY = "ts.card.accessory"
    const val FOOTER = "ts.card.footer"
}

@Composable
public fun TSCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    raised: Boolean = false,
    footerActions: List<TSCardAction> = emptyList(),
    accessibilityActions: List<TSCardAction> = emptyList(),
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    accessory: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    TSCardLayout(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        bodyText = null,
        raised = raised,
        footerActions = footerActions,
        accessibilityActions = accessibilityActions,
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        cardAccessory = null,
        accessory = accessory,
        content = content,
    )
}

@Composable
public fun TSCard(
    text: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    raised: Boolean = false,
    footerActions: List<TSCardAction> = emptyList(),
    accessibilityActions: List<TSCardAction> = emptyList(),
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    accessory: (@Composable () -> Unit)? = null,
) {
    TSCardLayout(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        bodyText = text,
        raised = raised,
        footerActions = footerActions,
        accessibilityActions = accessibilityActions,
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        cardAccessory = null,
        accessory = accessory,
    ) {
        TSCardBodyText(text)
    }
}

@Composable
public fun TSCard(
    accessory: TSCardAccessory,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    raised: Boolean = false,
    footerActions: List<TSCardAction> = emptyList(),
    accessibilityActions: List<TSCardAction> = emptyList(),
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: (@Composable () -> Unit)? = null,
) {
    TSCardLayout(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        bodyText = null,
        raised = raised,
        footerActions = footerActions,
        accessibilityActions = accessibilityActions,
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        cardAccessory = accessory,
        accessory = { TSCardAccessoryButton(accessory, enabled) },
        content = content,
    )
}

@Composable
public fun TSCard(
    text: String,
    accessory: TSCardAccessory,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    raised: Boolean = false,
    footerActions: List<TSCardAction> = emptyList(),
    accessibilityActions: List<TSCardAction> = emptyList(),
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    TSCardLayout(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        bodyText = text,
        raised = raised,
        footerActions = footerActions,
        accessibilityActions = accessibilityActions,
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        cardAccessory = accessory,
        accessory = { TSCardAccessoryButton(accessory, enabled) },
    ) {
        TSCardBodyText(text)
    }
}

@Composable
private fun TSCardAccessoryButton(accessory: TSCardAccessory, enabled: Boolean) {
    when (accessory) {
        is TSCardAccessory.IconButton -> TSIconButton(
            icon = accessory.icon,
            contentDescription = accessory.label,
            onClick = accessory.onClick,
            enabled = enabled,
        )
    }
}

@Composable
private fun TSCardLayout(
    title: String?,
    subtitle: String?,
    bodyText: String?,
    raised: Boolean,
    footerActions: List<TSCardAction>,
    accessibilityActions: List<TSCardAction>,
    onClick: (() -> Unit)?,
    enabled: Boolean,
    interactionSource: MutableInteractionSource?,
    cardAccessory: TSCardAccessory?,
    accessory: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)?,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val interactive = onClick != null
    val state = source.collectTSCardState(enabled)
    val resolved = TSCardStyleSpec.resolve(TSCardStyleSpec.Configuration(raised, interactive), state)
    val shape = RoundedCornerShape(resolved.cornerRadiusDp.dp)
    val label = TSCardAccessibility.label(title, subtitle, bodyText)
    val value = TSCardAccessibility.value(title, subtitle, bodyText)
    val clearsContent = interactive && label.isNotEmpty()
    val interaction = if (onClick != null) {
        val actions = TSCardAccessibility.customActions(footerActions, accessibilityActions, enabled, cardAccessory)
        Modifier
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = state.interactive,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics {
                if (label.isNotEmpty()) contentDescription = label
                if (value != null) stateDescription = value
                if (actions.isNotEmpty()) {
                    customActions = actions.map { action ->
                        CustomAccessibilityAction(action.label) {
                            action.onClick()
                            true
                        }
                    }
                }
            }
    } else {
        Modifier
    }

    Column(
        modifier = modifier
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
            .clip(shape)
            .then(interaction),
    ) {
        CompositionLocalProvider(LocalTSDimmedByContainer provides !enabled) {
            Column(
                modifier = Modifier
                    .testTag(TSCardTestTags.CONTENT)
                    .then(if (clearsContent) Modifier.clearAndSetSemantics {} else Modifier),
            ) {
                TSCardHeader(resolved, title, subtitle, hasBody = content != null, accessory = accessory)
                if (content != null) {
                    Box(Modifier.fillMaxWidth().padding(resolved.paddingDp.dp)) { content() }
                }
                if (footerActions.isNotEmpty()) {
                    TSCardFooter(resolved, footerActions, enabled)
                }
            }
        }
    }
}

@Composable
public fun TSCardBodyText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val resolved = TSCardStyleSpec.resolve(TSCardStyleSpec.Configuration())
    BasicText(
        text = text,
        modifier = modifier,
        style = TSTheme.typography.style(resolved.bodyTypeRole).copy(color = TSTheme.colors.color(resolved.bodyColor)).withContentDirectionAtLayoutStart(),
    )
}

@Composable
private fun MutableInteractionSource.collectTSCardState(enabled: Boolean): TSControlState {
    val base = collectTSControlState(enabled = enabled)
    val focused by collectIsFocusedAsState()
    return TSControlState.make(pressed = base.pressed, enabled = enabled, focused = base.focused || focused)
}

@Composable
private fun TSCardHeader(
    resolved: TSCardStyleSpec.Resolved,
    title: String?,
    subtitle: String?,
    hasBody: Boolean,
    accessory: (@Composable () -> Unit)?,
) {
    if (title == null && subtitle == null && accessory == null) return
    val colors = TSTheme.colors
    val typography = TSTheme.typography
    val padding = resolved.paddingDp.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = padding, end = padding, top = padding, bottom = if (hasBody) 0.dp else padding),
        horizontalArrangement = Arrangement.spacedBy(resolved.headerSpacingDp.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            if (title != null) {
                BasicText(
                    text = title,
                    modifier = Modifier.semantics { heading() },
                    style = typography.style(resolved.titleTypeRole).copy(color = colors.color(resolved.titleColor)).withContentDirectionAtLayoutStart(),
                )
            }
            if (subtitle != null) {
                if (title != null) Spacer(Modifier.height(resolved.subtitleSpacingDp.dp))
                BasicText(
                    text = subtitle,
                    style = typography.style(resolved.subtitleTypeRole).copy(color = colors.color(resolved.subtitleColor)).withContentDirectionAtLayoutStart(),
                )
            }
        }
        if (accessory != null) {
            Box(Modifier.testTag(TSCardTestTags.ACCESSORY)) { accessory() }
        }
    }
}

@Composable
private fun TSCardFooter(
    resolved: TSCardStyleSpec.Resolved,
    actions: List<TSCardAction>,
    enabled: Boolean,
) {
    val stacked = TSCardStyleSpec.stacksFooter(LocalDensity.current.fontScale)
    val padding = Modifier
        .fillMaxWidth()
        .padding(horizontal = resolved.footerHorizontalPaddingDp.dp, vertical = resolved.footerVerticalPaddingDp.dp)
    val spacing = resolved.footerSpacingDp.dp
    Box(
        Modifier
            .fillMaxWidth()
            .height(resolved.dividerThicknessDp.dp)
            .background(TSTheme.colors.color(resolved.divider)),
    )
    val buttons: @Composable () -> Unit = {
        actions.forEach { action ->
            TSButton(
                text = action.label,
                onClick = action.onClick,
                variant = action.variant,
                size = resolved.footerButtonSize,
                enabled = enabled,
            )
        }
    }
    if (stacked) {
        Column(
            modifier = padding.testTag(TSCardTestTags.FOOTER),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.End,
        ) { buttons() }
    } else {
        Row(
            modifier = padding.testTag(TSCardTestTags.FOOTER),
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) { buttons() }
    }
}
