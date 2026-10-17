package com.ts.ds.demo.views.shell

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.R
import com.ts.ds.views.components.dialog.TSDialogFragmentResults

abstract class DemoPageFragment(@LayoutRes layout: Int) : Fragment(layout) {
    protected abstract val page: DemoPageId

    protected val pageState = DemoPageState()
    protected val session: DemoSession
        get() = ViewModelProvider(requireActivity())[DemoSession::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageState.restore(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.tag = DemoTags.screen(page)
        setUpDemoScreen(
            root = view,
            title = getString(page.titleRes),
            menu = R.menu.menu_page,
            content = view.findViewById(R.id.page_scroll),
        )
        val rows = view.findViewById<LinearLayout>(R.id.page_rows)
        bindRows(rows)
        rows.assignStableIds()
        childFragmentManager.setFragmentResultListener(TSDialogFragmentResults.DROPPED, viewLifecycleOwner) { _, result ->
            result.getString(TSDialogFragmentResults.KEY_TAG)?.let(::representDialog)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pageState.save(outState)
    }

    protected abstract fun bindRows(rows: LinearLayout)

    protected open fun representDialog(tag: String) = Unit
}
