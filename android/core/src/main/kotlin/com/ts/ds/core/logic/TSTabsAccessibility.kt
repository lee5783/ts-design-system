package com.ts.ds.core.logic

import android.content.Context
import com.ts.ds.core.R

public object TSTabsAccessibility {
    public fun position(context: Context, index: Int, count: Int, includesRole: Boolean = true): String =
        context.getString(if (includesRole) R.string.ts_tab_position else R.string.ts_position_of_total, index + 1, count)
}
