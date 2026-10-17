package com.ts.ds.compose.foundation

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

public object TSFocusRingDefaults {
    public val Width: Dp = 2.dp
    public val Gap: Dp = 2.dp
}

public fun Modifier.tsFocusRing(
    interactionSource: InteractionSource,
    shape: Shape,
    color: Color,
    width: Dp = TSFocusRingDefaults.Width,
    gap: Dp = TSFocusRingDefaults.Gap,
): Modifier = this then TSFocusRingElement(interactionSource, shape, color, width, gap)

internal fun isFocusRingVisible(focused: Boolean, inputMode: InputMode): Boolean =
    focused && inputMode == InputMode.Keyboard

private data class TSFocusRingElement(
    val interactionSource: InteractionSource,
    val shape: Shape,
    val color: Color,
    val width: Dp,
    val gap: Dp,
) : ModifierNodeElement<TSFocusRingNode>() {
    override fun create(): TSFocusRingNode = TSFocusRingNode(interactionSource, shape, color, width, gap)

    override fun update(node: TSFocusRingNode) {
        node.update(interactionSource, shape, color, width, gap)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "tsFocusRing"
        properties["shape"] = shape
        properties["color"] = color
        properties["width"] = width
        properties["gap"] = gap
    }
}

private class TSFocusRingNode(
    private var interactionSource: InteractionSource,
    private var shape: Shape,
    private var color: Color,
    private var width: Dp,
    private var gap: Dp,
) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {
    private var focused = false
    private var focusJob: Job? = null

    override fun onAttach() {
        collectFocus()
    }

    fun update(interactionSource: InteractionSource, shape: Shape, color: Color, width: Dp, gap: Dp) {
        val sourceChanged = this.interactionSource != interactionSource
        this.interactionSource = interactionSource
        this.shape = shape
        this.color = color
        this.width = width
        this.gap = gap
        if (sourceChanged && isAttached) {
            focusJob?.cancel()
            focused = false
            collectFocus()
        }
        invalidateDraw()
    }

    private fun collectFocus() {
        val source = interactionSource
        focusJob = coroutineScope.launch {
            val focuses = mutableListOf<FocusInteraction.Focus>()
            source.interactions.collect { interaction ->
                when (interaction) {
                    is FocusInteraction.Focus -> focuses += interaction
                    is FocusInteraction.Unfocus -> focuses -= interaction.focus
                }
                focused = focuses.isNotEmpty()
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (!isFocusRingVisible(focused, currentValueOf(LocalInputModeManager).inputMode)) return
        drawFocusRing(shape, color, width, gap)
    }
}

internal fun Modifier.tsVisibleFocusRing(
    shape: Shape,
    color: Color,
    width: Dp = TSFocusRingDefaults.Width,
    gap: Dp = TSFocusRingDefaults.Gap,
): Modifier = drawWithContent {
    drawContent()
    drawFocusRing(shape, color, width, gap)
}

private fun ContentDrawScope.drawFocusRing(shape: Shape, color: Color, width: Dp, gap: Dp) {
    val strokePx = width.toPx()
    val inset = gap.toPx() + strokePx / 2f
    val ringSize = Size(size.width + inset * 2f, size.height + inset * 2f)
    val outline = shape.createOutline(ringSize, layoutDirection, this)
    translate(left = -inset, top = -inset) {
        drawOutline(outline, color, style = Stroke(width = strokePx))
    }
}
