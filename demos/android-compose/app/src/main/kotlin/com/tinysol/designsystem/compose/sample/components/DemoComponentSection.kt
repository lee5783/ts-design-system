package com.tinysol.designsystem.compose.sample.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.tinysol.designsystem.compose.theme.TSSpacing
import com.tinysol.designsystem.core.style.TSColorRole
import com.tinysol.designsystem.core.style.TSTypeRole
import com.tinysol.designsystem.compose.sample.shell.DemoSectionHeader
import com.tinysol.designsystem.compose.sample.shell.DemoText
import com.tinysol.designsystem.sample.support.R as DemoR

fun LazyListScope.componentSection(key: String, titleRes: Int, content: @Composable () -> Unit) {
    item(key = "$key.header") { DemoSectionHeader(text = stringResource(titleRes)) }
    item(key = key) {
        Column(verticalArrangement = Arrangement.spacedBy(TSSpacing.space3), modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun DemoComponentRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TSSpacing.space2),
        verticalArrangement = Arrangement.spacedBy(TSSpacing.space2),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

@Composable
fun DemoCaption(text: String, modifier: Modifier = Modifier) {
    DemoText(text = text, role = TSTypeRole.Caption, color = TSColorRole.TextSecondary, modifier = modifier)
}

@Composable
fun DemoTapCounter(count: Int, tag: String, modifier: Modifier = Modifier) {
    DemoText(
        text = pluralStringResource(DemoR.plurals.demo_component_tap_count, count, count),
        role = TSTypeRole.BodySm,
        color = TSColorRole.TextSecondary,
        modifier = modifier.testTag(tag),
    )
}
