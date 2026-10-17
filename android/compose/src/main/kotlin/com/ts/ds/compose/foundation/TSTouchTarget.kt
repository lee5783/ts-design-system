package com.ts.ds.compose.foundation

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

public object TSTouchTarget {
    public val MinSize: Dp = 48.dp
}

public fun Modifier.tsMinimumTouchTarget(size: Dp = TSTouchTarget.MinSize): Modifier =
    this then TSMinimumTouchTargetElement(size)

private data class TSMinimumTouchTargetElement(val size: Dp) : ModifierNodeElement<TSMinimumTouchTargetNode>() {
    override fun create(): TSMinimumTouchTargetNode = TSMinimumTouchTargetNode(size)

    override fun update(node: TSMinimumTouchTargetNode) {
        node.size = size
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "tsMinimumTouchTarget"
        properties["size"] = size
    }
}

private class TSMinimumTouchTargetNode(var size: Dp) : Modifier.Node(), LayoutModifierNode {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        val minPx = size.roundToPx()
        val width = maxOf(placeable.width, minPx).coerceAtMost(constraints.maxWidth)
        val height = maxOf(placeable.height, minPx).coerceAtMost(constraints.maxHeight)
        return layout(width, height) {
            placeable.place((width - placeable.width) / 2, (height - placeable.height) / 2)
        }
    }
}
