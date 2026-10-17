package com.ts.ds.views.snapshots

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSBottomNavItem
import com.ts.ds.views.components.bottomnav.TSBottomNavView

private val sampleItems = listOf(
    TSBottomNavItem("home", "Home", TSIcon.Home),
    TSBottomNavItem("inbox", "Inbox", TSIcon.Mail),
    TSBottomNavItem("search", "Search", TSIcon.Search),
    TSBottomNavItem("alerts", "Alerts", TSIcon.Bell),
    TSBottomNavItem("profile", "Profile", TSIcon.User),
)

private enum class BottomNavSampleState { Default, Pressed, Focused, Disabled }

private fun items(count: Int, inbox: Int? = null, alerts: Int? = null): List<TSBottomNavItem> {
    val badged = sampleItems.map {
        when (it.id) {
            "inbox" -> it.badged(inbox)
            "alerts" -> it.badged(alerts)
            else -> it
        }
    }
    return when (count) {
        3 -> listOf(badged[0], badged[1], badged[4])
        4 -> badged.take(4)
        else -> badged
    }
}

private fun LinearLayout.addNav(
    context: Context,
    items: List<TSBottomNavItem>,
    selected: String,
    state: BottomNavSampleState = BottomNavSampleState.Default,
) {
    val view = TSBottomNavView(context).apply {
        this.items = items
        selectedId = selected
        label = "Main navigation"
        reduceMotion = { true }
        isEnabled = state != BottomNavSampleState.Disabled
    }
    ViewCompat.setOnApplyWindowInsetsListener(view, null)
    addView(view, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    when (state) {
        BottomNavSampleState.Pressed -> view.itemViewList.filter { !it.isItemSelected }.forEach { it.isPressed = true }
        BottomNavSampleState.Focused -> view.itemViewList.first { it.isItemSelected }.apply {
            isFocusableInTouchMode = true
            post { requestFocus() }
        }
        else -> Unit
    }
}

internal fun bottomNavGallerySample(context: Context): View = context.galleryColumn().apply {
    addNav(context, items(3), "home")
    addNav(context, items(4, inbox = 3, alerts = 120), "inbox")
    addNav(context, items(5), "home", BottomNavSampleState.Pressed)
    addNav(context, items(5, inbox = 3, alerts = 120), "alerts", BottomNavSampleState.Focused)
    addNav(context, items(4, inbox = 3), "home", BottomNavSampleState.Disabled)
}
