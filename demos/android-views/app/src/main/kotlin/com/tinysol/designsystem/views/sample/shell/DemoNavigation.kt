package com.tinysol.designsystem.views.sample.shell

import android.content.Context
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.sample.support.settings.DemoSettingsStore
import com.tinysol.designsystem.views.sample.R
import com.tinysol.designsystem.views.sample.components.PendingComponentFragment

val DemoPageId.destinationId: Int
    get() = when (this) {
        DemoPageId.Colors -> R.id.page_colors
        DemoPageId.Typography -> R.id.page_typography
        DemoPageId.Spacing -> R.id.page_spacing
        DemoPageId.Radius -> R.id.page_radius
        DemoPageId.Size -> R.id.page_size
        DemoPageId.Opacity -> R.id.page_opacity
        DemoPageId.Elevation -> R.id.page_elevation
        DemoPageId.Icons -> R.id.page_icons
        DemoPageId.Button -> R.id.page_button
        DemoPageId.IconButton -> R.id.page_icon_button
        DemoPageId.Badge -> R.id.page_badge
        DemoPageId.Avatar -> R.id.page_avatar
        DemoPageId.TextField -> R.id.page_text_field
        DemoPageId.Checkbox -> R.id.page_checkbox
        DemoPageId.Switch -> R.id.page_switch
        DemoPageId.Tabs -> R.id.page_tabs
        DemoPageId.ListItem -> R.id.page_list_item
        DemoPageId.Card -> R.id.page_card
        DemoPageId.Alert -> R.id.page_alert
        DemoPageId.Dialog -> R.id.page_dialog
        DemoPageId.Toast -> R.id.page_toast
        DemoPageId.BottomNav -> R.id.page_bottom_nav
        DemoPageId.Spinner -> R.id.page_spinner
        DemoPageId.Text -> R.id.page_text
        else -> R.id.page_pending_component
    }

fun NavController.openPage(context: Context, page: DemoPageId) {
    navigate(
        page.destinationId,
        bundleOf(PendingComponentFragment.ARG_PAGE_ID to page.id),
        demoNavOptions(context),
    )
}

fun NavController.openDestination(context: Context, destinationId: Int) {
    navigate(destinationId, null, demoNavOptions(context))
}

private fun demoNavOptions(context: Context): NavOptions = navOptions {
    if (!DemoSettingsStore.from(context).settings.reduceMotion) {
        anim {
            enter = R.anim.demo_fade_enter
            exit = R.anim.demo_fade_exit
            popEnter = R.anim.demo_fade_enter
            popExit = R.anim.demo_fade_exit
        }
    }
}
