package com.tinysol.designsystem.compose.sample.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.components.button.TSButton
import com.tinysol.designsystem.compose.components.icon.TSIconSource
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.style.TSButtonSize
import com.tinysol.designsystem.core.style.TSButtonVariant
import com.tinysol.designsystem.compose.sample.R
import com.tinysol.designsystem.compose.sample.shell.OptionChip
import com.tinysol.designsystem.compose.sample.shell.OptionGroup
import com.tinysol.designsystem.compose.sample.shell.ToggleRow
import com.tinysol.designsystem.sample.support.R as DemoR

object ButtonPageTags {
    const val PLAYGROUND_BUTTON = "demo.button.playground.button"
    const val PLAYGROUND_COUNT = "demo.button.playground.count"
    const val PLAYGROUND_LOADING = "demo.button.playground.loading"
    const val PLAYGROUND_DISABLED = "demo.button.playground.disabled"
    const val LINK = "demo.button.variants.link"
    const val CONSUMER_ICON = "demo.button.consumer-icon"
    const val LINE_LIMIT = "demo.button.line-limit"

    fun variant(variant: TSButtonVariant): String = "demo.button.playground.variant.${variant.name.lowercase()}"
}

fun LazyListScope.buttonPage() {
    componentSection("button.variants", DemoR.string.demo_component_variants) {
        TSButton(stringResource(DemoR.string.demo_sample_save), onClick = {}, variant = TSButtonVariant.Primary)
        TSButton(stringResource(DemoR.string.demo_sample_cancel), onClick = {}, variant = TSButtonVariant.Secondary)
        TSButton(stringResource(DemoR.string.demo_sample_add_item), onClick = {}, variant = TSButtonVariant.Ghost, leadingIcon = TSIcon.Plus)
        TSButton(stringResource(DemoR.string.demo_sample_delete), onClick = {}, variant = TSButtonVariant.Danger, leadingIcon = TSIcon.Trash)
        TSButton(
            stringResource(DemoR.string.demo_sample_learn_more),
            onClick = {},
            variant = TSButtonVariant.Link,
            modifier = Modifier.testTag(ButtonPageTags.LINK),
        )
    }
    componentSection("button.sizes", DemoR.string.demo_component_sizes) {
        TSButton(stringResource(DemoR.string.demo_sample_continue), onClick = {}, size = TSButtonSize.Md, trailingIcon = TSIcon.ArrowRight)
        TSButton(stringResource(DemoR.string.demo_sample_get_started), onClick = {}, size = TSButtonSize.Lg)
        TSButton(stringResource(DemoR.string.demo_component_block), onClick = {}, size = TSButtonSize.Lg, modifier = Modifier.fillMaxWidth())
    }
    componentSection("button.states", DemoR.string.demo_component_states) {
        TSButtonVariant.entries.forEach { variant ->
            TSButtonSize.entries.forEach { size ->
                DemoCaption(text = "${variant.name.lowercase()} · ${size.name.lowercase()}")
                DemoComponentRow {
                    TSButton(stringResource(DemoR.string.demo_component_default), onClick = {}, variant = variant, size = size)
                    TSButton(stringResource(DemoR.string.demo_component_disabled), onClick = {}, variant = variant, size = size, enabled = false)
                    TSButton(stringResource(DemoR.string.demo_component_loading), onClick = {}, variant = variant, size = size, loading = true)
                }
            }
        }
    }
    componentSection("button.icons", DemoR.string.demo_component_with_icons) {
        TSButton(stringResource(DemoR.string.demo_component_leading_icon), onClick = {}, variant = TSButtonVariant.Secondary, leadingIcon = TSIcon.Download)
        TSButton(stringResource(DemoR.string.demo_component_trailing_icon), onClick = {}, variant = TSButtonVariant.Secondary, trailingIcon = TSIcon.ChevronRight)
        TSButton(
            stringResource(DemoR.string.demo_component_both_icons),
            onClick = {},
            variant = TSButtonVariant.Secondary,
            leadingIcon = TSIcon.Upload,
            trailingIcon = TSIcon.ArrowRight,
        )
    }
    componentSection("button.consumer-icon", DemoR.string.demo_component_consumer_icon) {
        val controller = TSIconSource.Vector(ImageVector.vectorResource(R.drawable.demo_ic_controller))
        DemoComponentRow {
            TSButton(
                stringResource(DemoR.string.demo_sample_play_together),
                onClick = {},
                leadingIconSource = controller,
                modifier = Modifier.testTag(ButtonPageTags.CONSUMER_ICON),
            )
            TSButton(stringResource(DemoR.string.demo_sample_play_together), onClick = {}, variant = TSButtonVariant.Secondary, leadingIconSource = controller)
            TSButton(stringResource(DemoR.string.demo_sample_play_together), onClick = {}, leadingIconSource = controller, loading = true)
        }
    }
    componentSection("button.line-limit", DemoR.string.demo_component_line_limit) {
        TSButton(
            stringResource(DemoR.string.demo_sample_join_tournament),
            onClick = {},
            variant = TSButtonVariant.Secondary,
            leadingIconSource = TSIconSource.Vector(ImageVector.vectorResource(R.drawable.demo_ic_controller)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .widthIn(max = LINE_LIMIT_WIDTH)
                .testTag(ButtonPageTags.LINE_LIMIT),
        )
    }
    componentSection("button.playground", DemoR.string.demo_component_playground) { ButtonPlayground() }
}

private val LINE_LIMIT_WIDTH = 240.dp

@Composable
private fun ButtonPlayground() {
    var variant by rememberSaveable { mutableStateOf(TSButtonVariant.Primary) }
    var size by rememberSaveable { mutableStateOf(TSButtonSize.Md) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var disabled by rememberSaveable { mutableStateOf(false) }
    var block by rememberSaveable { mutableStateOf(false) }
    var leading by rememberSaveable { mutableStateOf(false) }
    var trailing by rememberSaveable { mutableStateOf(false) }
    var taps by rememberSaveable { mutableIntStateOf(0) }

    DemoCaption(text = stringResource(DemoR.string.demo_component_variant))
    OptionGroup {
        TSButtonVariant.entries.forEach { option ->
            OptionChip(
                label = option.name.lowercase(),
                selected = variant == option,
                onClick = { variant = option },
                modifier = Modifier.testTag(ButtonPageTags.variant(option)),
            )
        }
    }
    DemoCaption(text = stringResource(DemoR.string.demo_component_size))
    OptionGroup {
        TSButtonSize.entries.forEach { option ->
            OptionChip(label = option.name.lowercase(), selected = size == option, onClick = { size = option })
        }
    }
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_loading),
        checked = loading,
        onCheckedChange = { loading = it },
        modifier = Modifier.testTag(ButtonPageTags.PLAYGROUND_LOADING),
    )
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_disabled),
        checked = disabled,
        onCheckedChange = { disabled = it },
        modifier = Modifier.testTag(ButtonPageTags.PLAYGROUND_DISABLED),
    )
    ToggleRow(label = stringResource(DemoR.string.demo_component_block), checked = block, onCheckedChange = { block = it })
    ToggleRow(label = stringResource(DemoR.string.demo_component_leading_icon), checked = leading, onCheckedChange = { leading = it })
    ToggleRow(label = stringResource(DemoR.string.demo_component_trailing_icon), checked = trailing, onCheckedChange = { trailing = it })
    TSButton(
        text = stringResource(DemoR.string.demo_sample_save),
        onClick = { taps++ },
        variant = variant,
        size = size,
        leadingIcon = if (leading) TSIcon.Check else null,
        trailingIcon = if (trailing) TSIcon.ArrowRight else null,
        enabled = !disabled,
        loading = loading,
        modifier = Modifier
            .then(if (block) Modifier.fillMaxWidth() else Modifier)
            .testTag(ButtonPageTags.PLAYGROUND_BUTTON),
    )
    DemoTapCounter(count = taps, tag = ButtonPageTags.PLAYGROUND_COUNT)
}
