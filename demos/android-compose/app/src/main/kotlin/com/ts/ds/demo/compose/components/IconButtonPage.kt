package com.ts.ds.demo.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.ts.ds.compose.components.icon.TSIconSource
import com.ts.ds.compose.components.iconbutton.TSIconButton
import com.ts.ds.compose.theme.TSSpacing
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSIconButtonVariant
import com.ts.ds.demo.compose.R
import com.ts.ds.demo.compose.shell.OptionChip
import com.ts.ds.demo.compose.shell.OptionGroup
import com.ts.ds.demo.compose.shell.ToggleRow
import com.ts.ds.demo.support.R as DemoR

object IconButtonPageTags {
    const val PLAYGROUND_BUTTON = "demo.icon-button.playground.button"
    const val PLAYGROUND_COUNT = "demo.icon-button.playground.count"
    const val PLAYGROUND_DISABLED = "demo.icon-button.playground.disabled"
    const val CONSUMER_ICON = "demo.icon-button.consumer-icon"
}

fun LazyListScope.iconButtonPage() {
    componentSection("icon-button.variants", DemoR.string.demo_component_variants) {
        TSIconButtonVariant.entries.forEach { variant ->
            Row(horizontalArrangement = Arrangement.spacedBy(TSSpacing.space3), verticalAlignment = Alignment.CenterVertically) {
                TSIconButton(TSIcon.Search, stringResource(DemoR.string.demo_sample_search), onClick = {}, variant = variant)
                DemoCaption(text = variant.name.lowercase())
            }
        }
    }
    componentSection("icon-button.shapes", DemoR.string.demo_component_shapes) {
        Row(horizontalArrangement = Arrangement.spacedBy(TSSpacing.space3)) {
            val add = stringResource(DemoR.string.demo_sample_add)
            TSIconButton(TSIcon.Plus, add, onClick = {}, variant = TSIconButtonVariant.Primary)
            TSIconButton(TSIcon.Plus, add, onClick = {}, variant = TSIconButtonVariant.Primary, round = true)
            TSIconButton(TSIcon.More, stringResource(DemoR.string.demo_sample_more_options), onClick = {}, variant = TSIconButtonVariant.Secondary, round = true)
        }
    }
    componentSection("icon-button.consumer-icon", DemoR.string.demo_component_consumer_icon) {
        Row(horizontalArrangement = Arrangement.spacedBy(TSSpacing.space3)) {
            val controller = TSIconSource.Vector(ImageVector.vectorResource(R.drawable.demo_ic_controller))
            val label = stringResource(DemoR.string.demo_sample_controller)
            TSIconButton(controller, label, onClick = {}, modifier = Modifier.testTag(IconButtonPageTags.CONSUMER_ICON))
            TSIconButton(controller, label, onClick = {}, variant = TSIconButtonVariant.Secondary)
            TSIconButton(controller, label, onClick = {}, variant = TSIconButtonVariant.Primary, round = true)
        }
    }
    componentSection("icon-button.states", DemoR.string.demo_component_states) {
        TSIconButtonVariant.entries.forEach { variant ->
            listOf(false, true).forEach { round ->
                val name = variant.name.lowercase()
                DemoCaption(text = if (round) "$name · ${stringResource(DemoR.string.demo_component_round)}" else name)
                Row(horizontalArrangement = Arrangement.spacedBy(TSSpacing.space3)) {
                    val close = stringResource(DemoR.string.demo_sample_close)
                    TSIconButton(TSIcon.Close, close, onClick = {}, variant = variant, round = round)
                    TSIconButton(TSIcon.Close, close, onClick = {}, variant = variant, round = round, enabled = false)
                }
            }
        }
    }
    componentSection("icon-button.playground", DemoR.string.demo_component_playground) { IconButtonPlayground() }
}

@Composable
private fun IconButtonPlayground() {
    var variant by rememberSaveable { mutableStateOf(TSIconButtonVariant.Ghost) }
    var round by rememberSaveable { mutableStateOf(false) }
    var disabled by rememberSaveable { mutableStateOf(false) }
    var taps by rememberSaveable { mutableIntStateOf(0) }

    DemoCaption(text = stringResource(DemoR.string.demo_component_variant))
    OptionGroup {
        TSIconButtonVariant.entries.forEach { option ->
            OptionChip(label = option.name.lowercase(), selected = variant == option, onClick = { variant = option })
        }
    }
    ToggleRow(label = stringResource(DemoR.string.demo_component_round), checked = round, onCheckedChange = { round = it })
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_disabled),
        checked = disabled,
        onCheckedChange = { disabled = it },
        modifier = Modifier.testTag(IconButtonPageTags.PLAYGROUND_DISABLED),
    )
    TSIconButton(
        icon = TSIcon.Settings,
        contentDescription = stringResource(DemoR.string.demo_sample_more_options),
        onClick = { taps++ },
        variant = variant,
        round = round,
        enabled = !disabled,
        modifier = Modifier.testTag(IconButtonPageTags.PLAYGROUND_BUTTON),
    )
    DemoTapCounter(count = taps, tag = IconButtonPageTags.PLAYGROUND_COUNT)
}
