package com.ts.ds.compose.components.bottomnav

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.icon.TSIconImage
import com.ts.ds.compose.components.shared.collectTSControlState
import com.ts.ds.compose.components.shared.tsControlChrome
import com.ts.ds.compose.components.shared.tsReduceMotion
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.style
import com.ts.ds.compose.theme.withDsFontWeight
import com.ts.ds.core.logic.TSBottomNavAccessibility
import com.ts.ds.core.style.TSBottomNavItem
import com.ts.ds.core.style.TSBottomNavStyleSpec
import com.ts.ds.core.style.TSTypeRole

internal object TSBottomNavTestTags {
    fun item(id: String): String = "ts.bottom-nav.item.$id"

    fun pill(id: String): String = "ts.bottom-nav.pill.$id"

    fun label(id: String): String = "ts.bottom-nav.label.$id"

    fun badge(id: String): String = "ts.bottom-nav.badge.$id"
}

@Composable
public fun TSBottomNav(
    items: List<TSBottomNavItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    windowInsets: WindowInsets = WindowInsets.navigationBars,
) {
    val container = TSBottomNavStyleSpec.container
    val colors = TSTheme.colors
    val hairline = colors.color(container.border)
    val selectedIndex = TSBottomNavStyleSpec.selectedIndex(items, selectedId)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.color(container.fill))
            .drawBehind { drawRect(hairline, size = Size(size.width, container.borderWidthDp.dp.toPx())) }
            .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .height(container.heightDp.dp)
            .semantics {
                label?.let { contentDescription = it }
                collectionInfo = CollectionInfo(rowCount = 1, columnCount = items.size)
            }
            .selectableGroup(),
    ) {
        items.forEachIndexed { index, item ->
            key(item.id) {
                TSBottomNavCell(
                    item = item,
                    index = index,
                    count = items.size,
                    selected = index == selectedIndex,
                    enabled = enabled,
                    onClick = { onSelect(item.id) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.TSBottomNavCell(
    item: TSBottomNavItem,
    index: Int,
    count: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val state = source.collectTSControlState(enabled = enabled)
    val resolved = TSBottomNavStyleSpec.resolve(TSBottomNavStyleSpec.Configuration(selected), state)
    val context = LocalContext.current
    val value = TSBottomNavAccessibility.value(context, index, count, item.badge, includesRole = false)
    val badgeText = TSBottomNavAccessibility.badgeText(context, item.badge)
    val colors = TSTheme.colors
    val reduceMotion = tsReduceMotion()
    val curve = remember(resolved.animationEasing) {
        resolved.animationEasing.let { CubicBezierEasing(it.x1, it.y1, it.x2, it.y2) }
    }
    val spec: AnimationSpec<Color> = if (reduceMotion) snap() else tween(resolved.animationDurationMillis, easing = curve)
    val pill by animateColorAsState(resolved.pillFill?.let(colors::color) ?: Color.Transparent, spec, label = "ts.bottom-nav.pill")
    val foreground by animateColorAsState(colors.color(resolved.foreground), spec, label = "ts.bottom-nav.foreground")
    val pillShape = RoundedCornerShape(resolved.pillCornerRadiusDp.dp)
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .testTag(TSBottomNavTestTags.item(item.id))
            .selectable(
                selected = selected,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Tab,
                onClick = { if (!selected) onClick() },
            )
            .semantics {
                stateDescription = value
                collectionItemInfo = CollectionItemInfo(rowIndex = 0, rowSpan = 1, columnIndex = index, columnSpan = 1)
            }
            .alpha(resolved.opacity),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(resolved.spacingDp.dp),
        ) {
            Box(
                modifier = Modifier
                    .testTag(TSBottomNavTestTags.pill(item.id))
                    .size(resolved.pillWidthDp.dp, resolved.pillHeightDp.dp)
                    .tsControlChrome(
                        interactionSource = source,
                        shape = pillShape,
                        fill = null,
                        border = null,
                        borderWidthDp = 0f,
                        shadow = null,
                        opacity = 1f,
                        showsFocusRing = resolved.showsFocusRing,
                    )
                    .background(pill, pillShape),
                contentAlignment = Alignment.Center,
            ) {
                TSIconImage(item.icon, contentDescription = null, size = resolved.iconSizeDp.dp, tint = foreground)
                if (badgeText != null) {
                    TSBottomNavBadge(
                        text = badgeText,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(
                                x = (resolved.pillWidthDp / 2f + TSBottomNavStyleSpec.badge.offsetStartDp).dp,
                                y = TSBottomNavStyleSpec.badge.topFromPillDp.dp,
                            )
                            .testTag(TSBottomNavTestTags.badge(item.id)),
                    )
                }
            }
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, TSBottomNavStyleSpec.labelFontScale(density.fontScale)),
            ) {
                BasicText(
                    text = item.label,
                    modifier = Modifier
                        .testTag(TSBottomNavTestTags.label(item.id))
                        .padding(horizontal = 2.dp),
                    style = TSTheme.typography.style(resolved.labelTypeRole)
                        .withDsFontWeight(resolved.labelFontWeight)
                        .copy(color = foreground, textAlign = TextAlign.Center, textDirection = TextDirection.Content),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun TSBottomNavBadge(text: String, modifier: Modifier = Modifier) {
    val badge = TSBottomNavStyleSpec.badge
    val colors = TSTheme.colors
    val shape = RoundedCornerShape(badge.cornerRadiusDp.dp)
    val density = LocalDensity.current
    val style = with(density) {
        TSTheme.typography.style(TSTypeRole.Caption)
            .withDsFontWeight(badge.fontWeight)
            .copy(
                color = colors.color(badge.foreground),
                fontSize = badge.fontSizeDp.dp.toSp(),
                lineHeight = badge.heightDp.dp.toSp(),
                textAlign = TextAlign.Center,
            )
    }
    Box(
        modifier = modifier
            .clearAndSetSemantics {}
            .defaultMinSize(minWidth = badge.minWidthDp.dp)
            .height(badge.heightDp.dp)
            .background(colors.color(badge.fill), shape)
            .padding(horizontal = badge.horizontalPaddingDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(text = text, style = style, maxLines = 1, softWrap = false)
    }
}
