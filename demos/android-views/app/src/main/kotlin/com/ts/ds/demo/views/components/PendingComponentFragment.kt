package com.ts.ds.demo.views.components

import android.widget.LinearLayout
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment

class PendingComponentFragment : DemoPageFragment(R.layout.page_pending) {
    override val page: DemoPageId
        get() = requireNotNull(DemoPageId.fromId(requireArguments().getString(ARG_PAGE_ID).orEmpty()))

    override fun bindRows(rows: LinearLayout) = Unit

    companion object {
        const val ARG_PAGE_ID = "pageId"
    }
}
