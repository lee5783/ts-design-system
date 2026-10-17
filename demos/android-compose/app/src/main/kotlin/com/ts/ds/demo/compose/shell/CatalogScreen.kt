package com.ts.ds.demo.compose.shell

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.ts.ds.compose.components.listitem.TSListItem
import com.ts.ds.compose.components.listitem.TSListSection
import com.ts.ds.compose.theme.TSSpacing
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoCatalog
import com.ts.ds.demo.support.catalog.DemoPageId

@Composable
fun CatalogScreen(
    onOpenPage: (DemoPageId) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DemoScreen(
        title = stringResource(DemoR.string.demo_app_title),
        modifier = modifier.testTag(DemoTags.CATALOG),
        actions = {
            DemoIconAction(
                icon = TSIcon.Settings,
                contentDescription = stringResource(DemoR.string.demo_action_settings),
                onClick = onOpenSettings,
                modifier = Modifier.testTag(DemoTags.ACTION_SETTINGS),
            )
            DemoIconAction(
                icon = TSIcon.Info,
                contentDescription = stringResource(DemoR.string.demo_action_about),
                onClick = onOpenAbout,
                modifier = Modifier.testTag(DemoTags.ACTION_ABOUT),
            )
        },
    ) {
        DemoCatalog.sections.forEach { section ->
            item(key = section.id.id) {
                TSListSection(
                    header = stringResource(section.id.titleRes),
                    modifier = Modifier.padding(top = TSSpacing.space3),
                ) {
                    section.pages.forEach { page ->
                        TSListItem(
                            title = stringResource(page.titleRes),
                            onClick = { onOpenPage(page) },
                            showsChevron = true,
                            modifier = Modifier.testTag(page.testTag),
                        )
                    }
                }
            }
        }
    }
}
