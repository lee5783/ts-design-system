package com.ts.ds.demo.compose.shell

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.demo.compose.components.alertPage
import com.ts.ds.demo.compose.components.avatarPage
import com.ts.ds.demo.compose.components.badgePage
import com.ts.ds.demo.compose.components.bottomNavPage
import com.ts.ds.demo.compose.components.buttonPage
import com.ts.ds.demo.compose.components.cardPage
import com.ts.ds.demo.compose.components.checkboxPage
import com.ts.ds.demo.compose.components.dialogPage
import com.ts.ds.demo.compose.components.iconButtonPage
import com.ts.ds.demo.compose.components.listItemPage
import com.ts.ds.demo.compose.components.pendingComponentPage
import com.ts.ds.demo.compose.components.spinnerPage
import com.ts.ds.demo.compose.components.switchPage
import com.ts.ds.demo.compose.components.tabsPage
import com.ts.ds.demo.compose.components.textFieldPage
import com.ts.ds.demo.compose.components.textPage
import com.ts.ds.demo.compose.components.toastPage
import com.ts.ds.demo.compose.foundations.colorsPage
import com.ts.ds.demo.compose.foundations.elevationPage
import com.ts.ds.demo.compose.foundations.iconsPage
import com.ts.ds.demo.compose.foundations.opacityPage
import com.ts.ds.demo.compose.foundations.radiusPage
import com.ts.ds.demo.compose.foundations.sizePage
import com.ts.ds.demo.compose.foundations.spacingPage
import com.ts.ds.demo.compose.foundations.typographyPage
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.catalog.DemoPageKind

@Composable
fun DemoPageScreen(
    page: DemoPageId,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenBottomNavShell: () -> Unit = {},
) {
    DemoScreen(
        title = stringResource(page.titleRes),
        onBack = onBack,
        modifier = modifier.testTag(DemoTags.screen(page)),
        actions = {
            DemoIconAction(
                icon = TSIcon.Settings,
                contentDescription = stringResource(DemoR.string.demo_action_settings),
                onClick = onOpenSettings,
                modifier = Modifier.testTag(DemoTags.ACTION_SETTINGS),
            )
        },
    ) {
        pageContent(page, onOpenBottomNavShell)
    }
}

private fun LazyListScope.pageContent(page: DemoPageId, onOpenBottomNavShell: () -> Unit) {
    when (page) {
        DemoPageId.Colors -> colorsPage()
        DemoPageId.Typography -> typographyPage()
        DemoPageId.Spacing -> spacingPage()
        DemoPageId.Radius -> radiusPage()
        DemoPageId.Size -> sizePage()
        DemoPageId.Opacity -> opacityPage()
        DemoPageId.Elevation -> elevationPage()
        DemoPageId.Icons -> iconsPage()
        DemoPageId.Button -> buttonPage()
        DemoPageId.IconButton -> iconButtonPage()
        DemoPageId.Badge -> badgePage()
        DemoPageId.Avatar -> avatarPage()
        DemoPageId.TextField -> textFieldPage()
        DemoPageId.Checkbox -> checkboxPage()
        DemoPageId.Switch -> switchPage()
        DemoPageId.Tabs -> tabsPage()
        DemoPageId.ListItem -> listItemPage()
        DemoPageId.Card -> cardPage()
        DemoPageId.Alert -> alertPage()
        DemoPageId.Dialog -> dialogPage()
        DemoPageId.Toast -> toastPage()
        DemoPageId.BottomNav -> bottomNavPage(onOpenBottomNavShell)
        DemoPageId.Spinner -> spinnerPage()
        DemoPageId.Text -> textPage()
        else -> {
            check(page.kind == DemoPageKind.Component)
            pendingComponentPage(page)
        }
    }
}
