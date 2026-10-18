package com.tinysol.designsystem.compose.sample.foundations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.tinysol.designsystem.compose.components.icon.TSIcon
import com.tinysol.designsystem.compose.theme.TSSpacing
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.theme.color
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.icon.dsName
import com.tinysol.designsystem.core.style.TSColorRole
import com.tinysol.designsystem.core.style.TSIconSize
import com.tinysol.designsystem.compose.sample.components.DemoCaption
import com.tinysol.designsystem.compose.sample.components.componentSection
import com.tinysol.designsystem.compose.sample.shell.DemoSectionHeader
import com.tinysol.designsystem.compose.sample.shell.DemoTokenRow
import com.tinysol.designsystem.sample.support.R as DemoR

fun LazyListScope.iconsPage() {
    componentSection("icons.sizes", DemoR.string.demo_component_sizes) {
        TSIconSize.entries.forEach { size ->
            Row(horizontalArrangement = Arrangement.spacedBy(TSSpacing.space3), verticalAlignment = Alignment.CenterVertically) {
                listOf(TSIcon.Home, TSIcon.Search, TSIcon.ChevronRight, TSIcon.ArrowLeft).forEach { icon ->
                    TSIcon(icon, size = size)
                }
                DemoCaption(text = "${size.name.lowercase()} · ${size.dp.toInt()}dp")
            }
        }
    }
    componentSection("icons.labelled", DemoR.string.demo_component_label) {
        Row(horizontalArrangement = Arrangement.spacedBy(TSSpacing.space3)) {
            listOf(
                Triple(TSIcon.CheckCircle, TSColorRole.Success, DemoR.string.demo_sample_success),
                Triple(TSIcon.Alert, TSColorRole.Warning, DemoR.string.demo_sample_warning),
                Triple(TSIcon.XCircle, TSColorRole.Danger, DemoR.string.demo_sample_error),
                Triple(TSIcon.Info, TSColorRole.Info, DemoR.string.demo_sample_information),
            ).forEach { (icon, role, _) ->
                TSIcon(icon, size = TSIconSize.Lg, tint = TSTheme.colors.color(role))
            }
        }
    }
    item(key = "icons.all.header") { DemoSectionHeader(text = stringResource(DemoR.string.demo_icons_all)) }
    items(TSIcon.entries, key = { it.name }) { icon ->
        DemoTokenRow(
            name = icon.name,
            value = if (icon.autoMirrored) "${icon.dsName} · mirrored" else icon.dsName,
        ) {
            TSIcon(icon, size = TSIconSize.Lg)
        }
    }
}
