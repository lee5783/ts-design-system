package com.tinysol.designsystem.views.foundation

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tinysol.designsystem.views.R

public fun View.applySystemBarInsetsAsPadding(
    start: Boolean = false,
    top: Boolean = false,
    end: Boolean = false,
    bottom: Boolean = false,
    insetTypes: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
) {
    val initial = getTag(R.id.ts_initial_padding) as? Insets
        ?: Insets.of(paddingStart, paddingTop, paddingEnd, paddingBottom).also { setTag(R.id.ts_initial_padding, it) }
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(insetTypes)
        val isRtl = view.layoutDirection == View.LAYOUT_DIRECTION_RTL
        val startInset = if (isRtl) insets.right else insets.left
        val endInset = if (isRtl) insets.left else insets.right
        view.setPaddingRelative(
            initial.left + if (start) startInset else 0,
            initial.top + if (top) insets.top else 0,
            initial.right + if (end) endInset else 0,
            initial.bottom + if (bottom) insets.bottom else 0,
        )
        windowInsets
    }
    ViewCompat.requestApplyInsets(this)
}
