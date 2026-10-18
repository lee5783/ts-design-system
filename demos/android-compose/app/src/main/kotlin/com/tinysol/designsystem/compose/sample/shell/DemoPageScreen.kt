package com.tinysol.designsystem.compose.sample.shell

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.compose.sample.components.alertPage
import com.tinysol.designsystem.compose.sample.components.avatarPage
import com.tinysol.designsystem.compose.sample.components.badgePage
import com.tinysol.designsystem.compose.sample.components.bottomNavPage
import com.tinysol.designsystem.compose.sample.components.buttonPage
import com.tinysol.designsystem.compose.sample.components.cardPage
import com.tinysol.designsystem.compose.sample.components.checkboxPage
import com.tinysol.designsystem.compose.sample.components.dialogPage
import com.tinysol.designsystem.compose.sample.components.iconButtonPage
import com.tinysol.designsystem.compose.sample.components.listItemPage
import com.tinysol.designsystem.compose.sample.components.pendingComponentPage
import com.tinysol.designsystem.compose.sample.components.spinnerPage
import com.tinysol.designsystem.compose.sample.components.switchPage
import com.tinysol.designsystem.compose.sample.components.tabsPage
import com.tinysol.designsystem.compose.sample.components.textFieldPage
import com.tinysol.designsystem.compose.sample.components.textPage
import com.tinysol.designsystem.compose.sample.components.toastPage
import com.tinysol.designsystem.compose.sample.foundations.colorsPage
import com.tinysol.designsystem.compose.sample.foundations.elevationPage
import com.tinysol.designsystem.compose.sample.foundations.iconsPage
import com.tinysol.designsystem.compose.sample.foundations.opacityPage
import com.tinysol.designsystem.compose.sample.foundations.radiusPage
import com.tinysol.designsystem.compose.sample.foundations.sizePage
import com.tinysol.designsystem.compose.sample.foundations.spacingPage
import com.tinysol.designsystem.compose.sample.foundations.typographyPage
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.sample.support.catalog.DemoPageKind

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
