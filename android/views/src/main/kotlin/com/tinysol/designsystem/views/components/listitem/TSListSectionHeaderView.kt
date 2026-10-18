package com.tinysol.designsystem.views.components.listitem

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.textview.MaterialTextView
import com.tinysol.designsystem.core.style.TSListItemStyleSpec
import com.tinysol.designsystem.views.components.shared.dpToPx
import com.tinysol.designsystem.views.theme.textAppearance
import com.tinysol.designsystem.views.theme.tsColor

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
    }
}
