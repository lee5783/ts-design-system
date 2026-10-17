package com.ts.ds.demo.compose.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.ts.ds.compose.theme.TSSpacing
import com.ts.ds.demo.compose.shell.ToggleRow
import com.ts.ds.demo.support.R as DemoR

typealias DemoToggleControl = @Composable (checked: Boolean, label: String, onCheckedChange: (Boolean) -> Unit, modifier: Modifier, labelHidden: Boolean, enabled: Boolean) -> Unit

class DemoTogglePageSpec(
    val page: String,
    @StringRes val stateSampleRes: Int,
    @StringRes val firstSampleRes: Int,
    @StringRes val secondSampleRes: Int,
    @StringRes val wrappingSampleRes: Int,
    @StringRes val onRes: Int,
    @StringRes val offRes: Int,
    val control: DemoToggleControl,
) {
    val playgroundControlTag: String get() = "demo.$page.playground.$page"
    val playgroundDisabledTag: String get() = "demo.$page.playground.disabled"
    val playgroundLabelHiddenTag: String get() = "demo.$page.playground.label-hidden"
    val playgroundValueTag: String get() = "demo.$page.playground.value"
    val playgroundCountTag: String get() = "demo.$page.playground.count"
}

fun LazyListScope.togglePage(spec: DemoTogglePageSpec) {
    componentSection("${spec.page}.states", DemoR.string.demo_component_states) { ToggleStates(spec) }
    componentSection("${spec.page}.label-hidden", DemoR.string.demo_component_label_hidden) { ToggleLabelHidden(spec) }
    componentSection("${spec.page}.wrapping", DemoR.string.demo_component_wrapping) { ToggleWrapping(spec) }
    componentSection("${spec.page}.playground", DemoR.string.demo_component_playground) { TogglePlayground(spec) }
}

@Composable
private fun ToggleStates(spec: DemoTogglePageSpec) {
    listOf(false, true).forEach { disabled ->
        listOf(false, true).forEach { checked ->
            DemoComponentRow {
                spec.control(checked, stringResource(spec.stateSampleRes), {}, Modifier, false, !disabled)
                val value = stringResource(if (checked) spec.onRes else spec.offRes)
                DemoCaption(text = if (disabled) stringResource(DemoR.string.demo_component_value_disabled, value) else value)
            }
        }
    }
    listOf(spec.firstSampleRes to false, spec.secondSampleRes to true).forEach { (label, initial) ->
        var checked by rememberSaveable(label) { mutableStateOf(initial) }
        spec.control(checked, stringResource(label), { checked = it }, Modifier, false, true)
    }
}

@Composable
private fun ToggleLabelHidden(spec: DemoTogglePageSpec) {
    Row(horizontalArrangement = Arrangement.spacedBy(TSSpacing.space4)) {
        listOf(spec.firstSampleRes to false, spec.secondSampleRes to true).forEach { (label, initial) ->
            var checked by rememberSaveable(label) { mutableStateOf(initial) }
            spec.control(checked, stringResource(label), { checked = it }, Modifier, true, true)
        }
    }
}

@Composable
private fun ToggleWrapping(spec: DemoTogglePageSpec) {
    var checked by rememberSaveable { mutableStateOf(true) }
    spec.control(checked, stringResource(spec.wrappingSampleRes), { checked = it }, Modifier, false, true)
}

@Composable
private fun TogglePlayground(spec: DemoTogglePageSpec) {
    var checked by rememberSaveable { mutableStateOf(false) }
    var disabled by rememberSaveable { mutableStateOf(false) }
    var labelHidden by rememberSaveable { mutableStateOf(false) }
    var changes by rememberSaveable { mutableIntStateOf(0) }

    ToggleRow(
        label = stringResource(DemoR.string.demo_component_disabled),
        checked = disabled,
        onCheckedChange = { disabled = it },
        modifier = Modifier.testTag(spec.playgroundDisabledTag),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_label_hidden),
        checked = labelHidden,
        onCheckedChange = { labelHidden = it },
        modifier = Modifier.testTag(spec.playgroundLabelHiddenTag),
    )
    spec.control(
        checked,
        stringResource(spec.secondSampleRes),
        {
            checked = it
            changes++
        },
        Modifier.testTag(spec.playgroundControlTag),
        labelHidden,
        !disabled,
    )
    DemoCaption(text = stringResource(if (checked) spec.onRes else spec.offRes), modifier = Modifier.testTag(spec.playgroundValueTag))
    DemoTapCounter(count = changes, tag = spec.playgroundCountTag)
}
