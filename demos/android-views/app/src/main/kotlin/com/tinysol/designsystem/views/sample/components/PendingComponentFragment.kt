package com.tinysol.designsystem.views.sample.components

import android.widget.LinearLayout
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.views.sample.R
import com.tinysol.designsystem.views.sample.shell.DemoPageFragment

class PendingComponentFragment : DemoPageFragment(R.layout.page_pending) {
    override val page: DemoPageId
        get() = requireNotNull(DemoPageId.fromId(requireArguments().getString(ARG_PAGE_ID).orEmpty()))

    override fun bindRows(rows: LinearLayout) = Unit

    companion object {
        const val ARG_PAGE_ID = "pageId"
    }
}
