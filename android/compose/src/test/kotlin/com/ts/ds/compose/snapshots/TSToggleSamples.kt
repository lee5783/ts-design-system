package com.ts.ds.compose.snapshots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.checkbox.TSCheckbox
import com.ts.ds.compose.components.shared.LocalTSForcedControlState
import com.ts.ds.compose.components.switchcontrol.TSSwitch
import com.ts.ds.core.style.TSControlState

internal const val SAMPLE_LONG_CONSENT = "Email me about product updates, tips and occasional offers from the team"
internal const val SAMPLE_LONG_SETTING = "Download updates automatically over mobile data when Wi-Fi is not available"

private enum class ToggleSampleState { Default, Pressed, Disabled, Focused }

@Composable
private fun ForcedToggle(state: ToggleSampleState, content: @Composable () -> Unit) {
    val forced = when (state) {
        ToggleSampleState.Pressed -> TSControlState.Pressed
        ToggleSampleState.Focused -> TSControlState.Focused
        else -> null
    }
    CompositionLocalProvider(LocalTSForcedControlState provides forced, content = content)
}

private fun sampleLabel(checked: Boolean, state: ToggleSampleState): String = "${if (checked) "On" else "Off"} ${state.name.lowercase()}"

@Composable
internal fun CheckboxStatesSample() {
    SampleColumn {
        listOf(false, true).forEach { checked ->
            ToggleSampleState.entries.forEach { state ->
                ForcedToggle(state) {
                    TSCheckbox(checked, sampleLabel(checked, state), {}, enabled = state != ToggleSampleState.Disabled)
                }
            }
        }
        TSCheckbox(true, SAMPLE_LONG_CONSENT, {})
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TSCheckbox(false, "Hidden off", {}, labelHidden = true)
            TSCheckbox(true, "Hidden on", {}, labelHidden = true)
        }
    }
}

@Composable
internal fun SwitchStatesSample() {
    SampleColumn {
        listOf(false, true).forEach { checked ->
            ToggleSampleState.entries.forEach { state ->
                ForcedToggle(state) {
                    TSSwitch(checked, sampleLabel(checked, state), {}, enabled = state != ToggleSampleState.Disabled)
                }
            }
        }
        TSSwitch(true, SAMPLE_LONG_SETTING, {})
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TSSwitch(false, "Hidden off", {}, labelHidden = true)
            TSSwitch(true, "Hidden on", {}, labelHidden = true)
        }
    }
}
