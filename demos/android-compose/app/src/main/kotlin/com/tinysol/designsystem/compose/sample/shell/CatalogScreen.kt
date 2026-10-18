package com.tinysol.designsystem.compose.sample.shell

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.tinysol.designsystem.compose.components.listitem.TSListItem
import com.tinysol.designsystem.compose.components.listitem.TSListSection
import com.tinysol.designsystem.compose.theme.TSSpacing
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoCatalog
import com.tinysol.designsystem.sample.support.catalog.DemoPageId

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
