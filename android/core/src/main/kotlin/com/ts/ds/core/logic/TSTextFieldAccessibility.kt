package com.ts.ds.core.logic

import android.content.Context
import com.ts.ds.core.R

public object TSTextFieldAccessibility {
    public fun hasError(error: String?): Boolean = !error.isNullOrBlank()

    public fun hint(hint: String?, error: String?): String? =
        hint?.takeIf { it.isNotBlank() && !hasError(error) }

    public fun errorDescription(context: Context, error: String): String =
        context.getString(R.string.ts_text_field_error, error)

    public fun toggleLabel(context: Context, revealed: Boolean): String =
        context.getString(if (revealed) R.string.ts_text_field_hide_password else R.string.ts_text_field_show_password)
}
