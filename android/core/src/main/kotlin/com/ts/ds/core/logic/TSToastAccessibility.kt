package com.ts.ds.core.logic

import android.content.Context
import com.ts.ds.core.R

public object TSToastAccessibility {
    public fun toneName(context: Context, tone: TSToastTone): String? = when (tone) {
        TSToastTone.Neutral -> null
        TSToastTone.Info -> context.getString(R.string.ts_alert_info)
        TSToastTone.Success -> context.getString(R.string.ts_alert_success)
        TSToastTone.Warning -> context.getString(R.string.ts_alert_warning)
        TSToastTone.Danger -> context.getString(R.string.ts_alert_danger)
    }

    public fun label(context: Context, tone: TSToastTone, message: CharSequence): String =
        listOfNotNull(toneName(context, tone), message)
            .filter { it.isNotEmpty() }
            .joinToString(", ")

    public fun announcement(context: Context, message: TSToastMessage): String =
        listOfNotNull(label(context, message.tone, message.text), message.actionLabel)
            .filter { it.isNotEmpty() }
            .joinToString(", ")
}
