package com.tinysol.designsystem.compose.components.shared

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp

internal const val SPINNER_START_ANGLE: Float = 45f
internal const val SPINNER_SWEEP_ANGLE: Float = 270f

@Composable
internal fun TSSpinner(
    color: Color,
    size: Dp,
    strokeWidth: Dp,
    rotationMillis: Int,
    modifier: Modifier = Modifier,
) {
    val rotation = if (LocalTSForcedControlState.current == null) {
        val transition = rememberInfiniteTransition(label = "tsSpinner")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(rotationMillis, easing = LinearEasing)),
            label = "tsSpinnerRotation",
        )
    } else {
        null
    }
    Canvas(modifier.size(size)) {
        val stroke = strokeWidth.toPx()
        rotate(rotation?.value ?: 0f) {
            drawArc(
                color = color,
                startAngle = SPINNER_START_ANGLE,
                sweepAngle = SPINNER_SWEEP_ANGLE,
                useCenter = false,
                topLeft = Offset(stroke / 2f, stroke / 2f),
                size = Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Butt),
            )
        }
    }
}
