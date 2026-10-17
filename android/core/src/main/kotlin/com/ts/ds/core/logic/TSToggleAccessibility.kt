package com.ts.ds.core.logic

import android.content.Context
import com.ts.ds.core.R

public object TSToggleAccessibility {
    public fun checkboxState(context: Context, checked: Boolean): String =
        context.getString(if (checked) R.string.ts_checkbox_checked else R.string.ts_checkbox_unchecked)

    public fun switchState(context: Context, checked: Boolean): String =
        context.getString(if (checked) R.string.ts_switch_on else R.string.ts_switch_off)
}
