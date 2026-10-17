package com.ts.ds.compose.components.switchcontrol

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.ts.ds.compose.components.shared.TSToggleRow
import com.ts.ds.compose.components.shared.collectTSControlState
import com.ts.ds.compose.components.shared.tsControlChrome
import com.ts.ds.compose.components.shared.tsReduceMotion
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.shadow
import com.ts.ds.core.R as CoreR
import com.ts.ds.core.style.TSControlState
import com.ts.ds.core.style.TSSwitchStyleSpec

internal object TSSwitchTestTags {
    const val TRACK = "ts.switch.track"
    const val THUMB = "ts.switch.thumb"
}

@Composable
public fun TSSwitch(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    labelHidden: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val state = source.collectTSControlState(enabled = enabled)
    val resolved = TSSwitchStyleSpec.resolve(TSSwitchStyleSpec.Configuration(checked), state)

    TSToggleRow(
        checked = checked,
        label = label,
        onCheckedChange = onCheckedChange,
        role = Role.Switch,
        stateDescription = stringResource(if (checked) CoreR.string.ts_switch_on else CoreR.string.ts_switch_off),
        labelHidden = labelHidden,
        enabled = state.interactive,
        labelTypeRole = resolved.labelTypeRole,
        labelColor = resolved.labelColor,
        spacingDp = resolved.spacingDp,
        minTouchTargetDp = resolved.minTouchTargetDp,
        opacity = resolved.opacity,
        interactionSource = source,
        modifier = modifier,
    ) {
        TSSwitchTrack(checked = checked, state = state, interactionSource = source)
    }
}

@Composable
internal fun TSSwitchTrack(
    checked: Boolean,
    state: TSControlState,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
) {
    val resolved = TSSwitchStyleSpec.resolve(TSSwitchStyleSpec.Configuration(checked), state)
    val off = TSSwitchStyleSpec.resolve(TSSwitchStyleSpec.Configuration(checked = false), state)
    val on = TSSwitchStyleSpec.resolve(TSSwitchStyleSpec.Configuration(checked = true), state)
    val easing = remember(resolved.animationEasing) {
        resolved.animationEasing.let { CubicBezierEasing(it.x1, it.y1, it.x2, it.y2) }
    }
    val animationSpec: AnimationSpec<Float> =
        if (tsReduceMotion()) snap() else tween(resolved.animationDurationMillis, easing = easing)
    val progress by animateFloatAsState(if (checked) 1f else 0f, animationSpec, label = "TSSwitchProgress")
    val colors = TSTheme.colors
    val trackColor = lerp(
        colors.color(off.trackColor).copy(alpha = off.trackOpacity),
        colors.color(on.trackColor).copy(alpha = on.trackOpacity),
        progress,
    )
    val thumbStart = lerp(off.thumbStartOffsetDp, on.thumbStartOffsetDp, progress)
    val thumbShadow = TSTheme.elevation.shadow(resolved.thumbShadow)
    val trackShape = RoundedCornerShape(resolved.trackCornerRadiusDp.dp)

    Box(
        modifier = modifier
            .testTag(TSSwitchTestTags.TRACK)
            .tsControlChrome(
                interactionSource = interactionSource,
                shape = trackShape,
                fill = null,
                border = null,
                borderWidthDp = 0f,
                shadow = null,
                opacity = 1f,
                showsFocusRing = resolved.showsFocusRing,
            )
            .background(trackColor, trackShape)
            .size(resolved.trackWidthDp.dp, resolved.trackHeightDp.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbStart.dp)
                .testTag(TSSwitchTestTags.THUMB)
                .size(resolved.thumbSizeDp.dp)
                .shadow(thumbShadow.elevation, CircleShape, clip = false, ambientColor = thumbShadow.color, spotColor = thumbShadow.color)
                .background(colors.color(resolved.thumbColor), CircleShape),
        )
    }
}
