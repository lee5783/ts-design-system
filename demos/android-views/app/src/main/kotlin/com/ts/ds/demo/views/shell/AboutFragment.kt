package com.ts.ds.demo.views.shell

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.ts.ds.demo.views.BuildConfig
import com.ts.ds.demo.views.R
import com.ts.ds.demo.support.R as DemoR

class AboutFragment : Fragment(R.layout.fragment_about) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.tag = DemoTags.ABOUT
        setUpDemoScreen(
            root = view,
            title = getString(DemoR.string.demo_about_title),
            menu = R.menu.menu_page,
            content = view.findViewById(R.id.page_scroll),
        )
        val rows = view.findViewById<LinearLayout>(R.id.page_rows)
        listOf(
            DemoR.string.demo_about_toolkit to getString(R.string.toolkit_name),
            DemoR.string.demo_about_app_version to BuildConfig.VERSION_NAME,
            DemoR.string.demo_about_library_version to BuildConfig.DS_LIBRARY_VERSION,
            DemoR.string.demo_about_design_version to BuildConfig.DS_DESIGN_VERSION,
        ).forEach { (label, value) -> rows.addTokenRow(name = getString(label), value = value) }
    }
}
