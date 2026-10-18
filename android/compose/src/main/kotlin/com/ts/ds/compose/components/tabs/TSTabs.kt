package com.ts.ds.compose.components.tabs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.shared.collectTSControlState
import com.ts.ds.compose.components.shared.tsControlChrome
import com.ts.ds.compose.components.shared.tsReduceMotion
import com.ts.ds.compose.foundation.rememberTSHaptics
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.shadow
import com.ts.ds.compose.theme.style
import com.ts.ds.core.haptics.DsHapticEvent
import com.ts.ds.core.style.TSTabItem
import com.ts.ds.core.style.TSTabsStyleSpec
import com.ts.ds.core.style.TSTabsVariant
import com.ts.ds.core.style.resolve
import com.ts.ds.tokens.TSCubicBezierValues
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal object TSTabsTestTags {
    const val INDICATOR = "ts.tabs.indicator"
    const val THUMB = "ts.tabs.thumb"

    fun tab(id: String): String = "ts.tabs.tab.$id"
}

@Stable
private class TSTabMarkerMotion(var index: Int) {
    val left = Animatable(0f)
    val width = Animatable(0f)
    var animating = false
    var pendingFrom: Pair<Int, Int>? = null
    var target: Pair<Int, Int>? = null
    var displayed: Pair<Int, Int>? = null

    fun current(target: Pair<Int, Int>): Pair<Int, Int> {
        this.target = target
        val animated = left.value.roundToInt() to width.value.roundToInt()
        val value = when {
            animating -> animated
            else -> pendingFrom ?: target
        }
        displayed = value
        return value
    }
}

@Composable
public fun TSTabs(
    items: List<TSTabItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: TSTabsVariant = TSTabsVariant.Underline,
    block: Boolean = false,
    enabled: Boolean = true,
) {
    val container = TSTabsStyleSpec.container(variant, enabled)
    val selectedIndex = TSTabsStyleSpec.selectedIndex(items, selectedId)
    val reference = TSTabsStyleSpec.resolve(TSTabsStyleSpec.Configuration(variant, selected = true))
    val haptics = rememberTSHaptics()
    val motion = rememberMarkerMotion(selectedIndex, reference.animationDurationMillis, reference.animationEasing)
    val groupModifier = Modifier
        .semantics {
        }
        .selectableGroup()
        .alpha(container.opacity)

    val tabs: @Composable () -> Unit = {
        items.forEachIndexed { index, item ->
            key(item.id) {
                TSTab(
                    item = item,
                    selected = index == selectedIndex,
                    variant = variant,
                    enabled = enabled,
                    onClick = {
                        if (variant == TSTabsVariant.Segmented) haptics.perform(DsHapticEvent.Selection)
                        onSelect(item.id)
                    },
                    selectedInView = variant == TSTabsVariant.Underline && index == selectedIndex,
                )
            }
        }
    }

    when (variant) {
        TSTabsVariant.Underline -> {
            val hairline = TSTheme.colors.color(container.border)
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .then(groupModifier)
                    .drawBehind {
                        val bottom = size.height - container.touchOutsetDp.dp.toPx()
                        val stroke = container.borderWidthDp.dp.toPx()
                        drawRect(hairline, Offset(0f, bottom - stroke), Size(size.width, stroke))
                    },
            ) {
                TSTabsRow(
                    selectedIndex = selectedIndex,
                    spacingDp = container.spacingDp,
                    equalWidths = false,
                    motion = motion,
                    marker = { UnderlineIndicator(reference) },
                    tabs = tabs,
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                )
            }
        }
        TSTabsVariant.Segmented -> {
            val colors = TSTheme.colors
            val fill = container.fill?.let(colors::color)
            val border = colors.color(container.border)
            Box(
                modifier = modifier
                    .then(if (block) Modifier.fillMaxWidth() else Modifier)
                    .then(groupModifier)
                    .drawBehind {
                        val outset = container.touchOutsetDp.dp.toPx()
                        val stroke = container.borderWidthDp.dp.toPx()
                        val radius = container.cornerRadiusDp.dp.toPx()
                        val trackSize = Size(size.width, size.height - outset * 2f)
                        if (fill != null) drawRoundRect(fill, Offset(0f, outset), trackSize, CornerRadius(radius))
                        drawRoundRect(
                            color = border,
                            topLeft = Offset(stroke / 2f, outset + stroke / 2f),
                            size = Size(trackSize.width - stroke, trackSize.height - stroke),
                            cornerRadius = CornerRadius(radius - stroke / 2f),
                            style = Stroke(stroke),
                        )
                    }
                    .padding(horizontal = container.contentInsetDp.dp),
            ) {
                TSTabsRow(
                    selectedIndex = selectedIndex,
                    spacingDp = container.spacingDp,
                    equalWidths = block,
                    motion = motion,
                    marker = { SegmentThumb(reference) },
                    tabs = tabs,
                    modifier = if (block) Modifier.fillMaxWidth() else Modifier,
                )
            }
        }
    }
}

@Composable
private fun TSTabsRow(
    selectedIndex: Int,
    spacingDp: Float,
    equalWidths: Boolean,
    motion: TSTabMarkerMotion,
    marker: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit,
) {
    Layout(contents = listOf(tabs, marker), modifier = modifier) { (tabMeasurables, markerMeasurables), constraints ->
        val spacing = spacingDp.dp.roundToPx()
        val count = tabMeasurables.size
        val gaps = spacing * (count - 1).coerceAtLeast(0)
        val tabConstraints = if (equalWidths && constraints.hasBoundedWidth && count > 0) {
            val width = ((constraints.maxWidth - gaps) / count).coerceAtLeast(0)
            Constraints(minWidth = width, maxWidth = width, maxHeight = constraints.maxHeight)
        } else {
            Constraints(maxHeight = constraints.maxHeight)
        }
        val placeables = tabMeasurables.map { it.measure(tabConstraints) }
        val contentWidth = placeables.sumOf { it.width } + gaps
        val width = if (equalWidths && constraints.hasBoundedWidth) constraints.maxWidth else contentWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = (placeables.maxOfOrNull { it.height } ?: 0).coerceIn(constraints.minHeight, constraints.maxHeight)
        val lefts = placeables.runningFold(0) { left, placeable -> left + placeable.width + spacing }
        val selected = placeables.getOrNull(selectedIndex)
        val markerPlaceable = selected?.let {
            val (left, markerWidth) = motion.current(lefts[selectedIndex] to it.width)
            markerMeasurables.firstOrNull()?.measure(Constraints.fixed(markerWidth.coerceAtLeast(0), height))?.let { placeable -> left to placeable }
        }
        layout(width, height) {
            markerPlaceable?.let { (left, placeable) -> placeable.placeRelative(left, 0) }
            placeables.forEachIndexed { index, placeable ->
                placeable.placeRelative(lefts[index], (height - placeable.height) / 2)
            }
        }
    }
}

@Composable
private fun TSTab(
    item: TSTabItem,
    selected: Boolean,
    variant: TSTabsVariant,
    enabled: Boolean,
    onClick: () -> Unit,
    selectedInView: Boolean,
) {
    val source = remember { MutableInteractionSource() }
    val state = source.collectTSControlState(enabled = enabled)
    val resolved = TSTabsStyleSpec.resolve(TSTabsStyleSpec.Configuration(variant, selected), state)
    val shape = RoundedCornerShape(resolved.cornerRadiusDp.dp)
    val requester = remember { BringIntoViewRequester() }
    if (selectedInView) {
        LaunchedEffect(Unit) { requester.bringIntoView() }
    }
    Box(
        modifier = Modifier
            .bringIntoViewRequester(requester)
            .testTag(TSTabsTestTags.tab(item.id))
            .selectable(
                selected = selected,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                onClick = { if (!selected) onClick() },
            )
            .heightIn(min = resolved.minTouchTargetDp.dp)
            .padding(vertical = resolved.touchOutsetDp.dp)
            .tsControlChrome(
                interactionSource = source,
                shape = shape,
                fill = if (selected) null else resolved.fill,
                border = null,
                borderWidthDp = 0f,
                shadow = null,
                opacity = 1f,
                showsFocusRing = resolved.showsFocusRing,
            )
            .heightIn(min = resolved.minHeightDp.dp)
            .padding(horizontal = resolved.paddingHorizontalDp.dp, vertical = resolved.paddingVerticalDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = item.label,
            style = TSTheme.typography.style(resolved.labelTypeRole).copy(color = TSTheme.colors.color(resolved.labelColor), textDirection = TextDirection.Content),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun UnderlineIndicator(reference: TSTabsStyleSpec.Resolved) {
    val color = reference.indicatorColor?.let(TSTheme.colors::color) ?: return
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = reference.touchOutsetDp.dp)
                .testTag(TSTabsTestTags.INDICATOR)
                .fillMaxWidth()
                .height(reference.indicatorHeightDp.dp)
                .background(color),
        )
    }
}

@Composable
private fun SegmentThumb(reference: TSTabsStyleSpec.Resolved) {
    val fill = reference.fill?.let(TSTheme.colors::color) ?: return
    val shape = RoundedCornerShape(reference.cornerRadiusDp.dp)
    val shadow = reference.shadow?.let { TSTheme.elevation.shadow(it) }
    Box(
        modifier = Modifier
            .padding(vertical = reference.touchOutsetDp.dp)
            .testTag(TSTabsTestTags.THUMB)
            .fillMaxSize()
            .then(
                if (shadow != null) {
                    Modifier.shadow(shadow.elevation, shape, clip = false, ambientColor = shadow.color, spotColor = shadow.color)
                } else {
                    Modifier
                },
            )
            .background(fill, shape),
    )
}

@Composable
private fun rememberMarkerMotion(selectedIndex: Int, durationMillis: Int, easing: TSCubicBezierValues): TSTabMarkerMotion {
    val motion = remember { TSTabMarkerMotion(selectedIndex) }
    val reduceMotion = tsReduceMotion()
    val curve = remember(easing) { CubicBezierEasing(easing.x1, easing.y1, easing.x2, easing.y2) }
    if (motion.index != selectedIndex) {
        motion.index = selectedIndex
        motion.pendingFrom = if (reduceMotion) null else motion.displayed
    }
    LaunchedEffect(selectedIndex) {
        val from = motion.pendingFrom ?: return@LaunchedEffect
        motion.left.snapTo(from.first.toFloat())
        motion.width.snapTo(from.second.toFloat())
        motion.animating = true
        motion.pendingFrom = null
        val spec = tween<Float>(durationMillis, easing = curve)
        try {
            coroutineScope {
                launch { motion.width.animateTo((motion.target ?: from).second.toFloat(), spec) }
                motion.left.animateTo((motion.target ?: from).first.toFloat(), spec)
            }
        } finally {
            motion.animating = false
        }
    }
    return motion
}
