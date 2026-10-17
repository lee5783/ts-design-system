package com.ts.ds.core.logic

import android.content.Context
import com.ts.ds.core.R
import com.ts.ds.core.style.TSAlertTone

public object TSAlertAccessibility {
    public fun toneName(context: Context, tone: TSAlertTone): String = context.getString(
        when (tone) {
            TSAlertTone.Info -> R.string.ts_alert_info
            TSAlertTone.Success -> R.string.ts_alert_success
            TSAlertTone.Warning -> R.string.ts_alert_warning
            TSAlertTone.Danger -> R.string.ts_alert_danger
        },
    )

    public fun label(context: Context, tone: TSAlertTone, title: CharSequence?, message: CharSequence?): String =
        listOfNotNull(toneName(context, tone), title, message)
            .filter { it.isNotEmpty() }
            .joinToString(", ")
}
