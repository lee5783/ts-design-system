package com.ts.ds.views.components.listitem

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import com.google.android.material.textview.MaterialTextView
import com.ts.ds.core.style.TSListItemStyleSpec
import com.ts.ds.views.components.shared.dpToPx
import com.ts.ds.views.theme.textAppearance
import com.ts.ds.views.theme.tsColor

public class TSListSectionHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MaterialTextView(context, attrs, defStyleAttr) {
    init {
        val group = TSListItemStyleSpec.group
        setTextAppearance(group.headerTypeRole.textAppearance)
        setTextColor(tsColor(group.headerColor))
        isAllCaps = true
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        val horizontal = dpToPx(group.headerHorizontalPaddingDp)
        setPaddingRelative(horizontal, 0, horizontal, dpToPx(group.headerSpacingDp))
        ViewCompat.setAccessibilityHeading(this, true)
    }
}
