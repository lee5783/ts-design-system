package com.tinysol.designsystem.compose.sample.components

import androidx.compose.foundation.lazy.LazyListScope
import com.tinysol.designsystem.compose.components.switchcontrol.TSSwitch
import com.tinysol.designsystem.sample.support.R as DemoR

val SwitchPageSpec = DemoTogglePageSpec(
    page = "switch",
    stateSampleRes = DemoR.string.demo_sample_wifi,
    firstSampleRes = DemoR.string.demo_sample_notifications,
    secondSampleRes = DemoR.string.demo_sample_dark_mode,
    wrappingSampleRes = DemoR.string.demo_sample_long_setting,
    onRes = DemoR.string.demo_state_on,
    offRes = DemoR.string.demo_state_off,
    control = { checked, label, onCheckedChange, modifier, labelHidden, enabled ->
        TSSwitch(checked, label, onCheckedChange, modifier, labelHidden = labelHidden, enabled = enabled)
    },
)

fun LazyListScope.switchPage() {
    togglePage(SwitchPageSpec)
}
