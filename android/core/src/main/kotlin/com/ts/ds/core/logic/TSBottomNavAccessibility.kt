package com.ts.ds.core.logic

import android.content.Context
import com.ts.ds.core.R

public object TSBottomNavAccessibility {
    public fun badgeText(context: Context, badge: Int?): String? =
        badge?.let { TSBadgeCountFormatter.format(context, it) }

    public fun value(context: Context, index: Int, count: Int, badge: Int?, includesRole: Boolean = true): String {
        val position = TSTabsAccessibility.position(context, index, count, includesRole)
        val text = badgeText(context, badge) ?: return position
        return "$position, ${context.getString(R.string.ts_bottom_nav_badge, text)}"
    }
}
