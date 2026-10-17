package com.ts.ds.compose.components.shared

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.foundation.rememberTSHaptics
import com.ts.ds.compose.foundation.tsMinimumTouchTarget
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.style
import com.ts.ds.core.haptics.DsHaptics
import com.ts.ds.core.motion.TSReduceMotion
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSTypeRole

@Suppress("ComposeCompositionLocalUsage")
internal val LocalTSReduceMotion = staticCompositionLocalOf<Boolean?> { null }

@Composable
internal fun tsReduceMotion(): Boolean = LocalTSReduceMotion.current ?: TSReduceMotion.isEnabled

@Composable
internal fun TSToggleRow(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit,
    role: Role,
    stateDescription: String,
    labelHidden: Boolean,
    enabled: Boolean,
    labelTypeRole: TSTypeRole,
    labelColor: TSColorRole,
    spacingDp: Float,
    minTouchTargetDp: Float,
    opacity: Float,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    control: @Composable () -> Unit,
) {
    val haptics = rememberTSHaptics()
    Row(
        modifier = modifier
            .toggleable(
                value = checked,
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = role,
                onValueChange = { value ->
                    haptics.perform(DsHaptics.toggleEvent(value))
                    onCheckedChange(value)
                },
            )
            .semantics {
                this.stateDescription = stateDescription
                if (labelHidden) contentDescription = label
            }
            .tsMinimumTouchTarget(minTouchTargetDp.dp)
            .alpha(opacity),
        horizontalArrangement = Arrangement.spacedBy(spacingDp.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        control()
        if (!labelHidden) {
            BasicText(
                text = label,
                style = TSTheme.typography.style(labelTypeRole).copy(color = TSTheme.colors.color(labelColor)),
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}
