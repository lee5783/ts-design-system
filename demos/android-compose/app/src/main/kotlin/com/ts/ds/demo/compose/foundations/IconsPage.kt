package com.ts.ds.demo.compose.foundations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.ts.ds.compose.components.icon.TSIcon
import com.ts.ds.compose.theme.TSSpacing
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.icon.dsName
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSIconSize
import com.ts.ds.demo.compose.components.DemoCaption
import com.ts.ds.demo.compose.components.componentSection
import com.ts.ds.demo.compose.shell.DemoSectionHeader
import com.ts.ds.demo.compose.shell.DemoTokenRow
import com.ts.ds.demo.support.R as DemoR

fun LazyListScope.iconsPage() {
    componentSection("icons.sizes", DemoR.string.demo_component_sizes) {
        TSIconSize.entries.forEach { size ->
            Row(horizontalArrangement = Arrangement.spacedBy(TSSpacing.space3), verticalAlignment = Alignment.CenterVertically) {
                listOf(TSIcon.Home, TSIcon.Search, TSIcon.ChevronRight, TSIcon.ArrowLeft).forEach { icon ->
                    TSIcon(icon, contentDescription = null, size = size)
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
            ).forEach { (icon, role, label) ->
                TSIcon(icon, contentDescription = stringResource(label), size = TSIconSize.Lg, tint = TSTheme.colors.color(role))
            }
        }
    }
    item(key = "icons.all.header") { DemoSectionHeader(text = stringResource(DemoR.string.demo_icons_all)) }
    items(TSIcon.entries, key = { it.name }) { icon ->
        DemoTokenRow(
            name = icon.name,
            value = if (icon.autoMirrored) "${icon.dsName} · mirrored" else icon.dsName,
        ) {
            TSIcon(icon, contentDescription = null, size = TSIconSize.Lg)
        }
    }
}
