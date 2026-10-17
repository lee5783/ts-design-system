package com.ts.ds.demo.views.shell

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.views.R
import com.ts.ds.views.components.listitem.TSListDecoration

class CatalogFragment : Fragment(R.layout.fragment_catalog) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.tag = DemoTags.CATALOG
        val list = view.findViewById<RecyclerView>(R.id.catalog_list)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = CatalogAdapter { page -> findNavController().openPage(requireContext(), page) }
        list.addItemDecoration(TSListDecoration(requireContext()) { CatalogItem.groupPosition(it) })
        setUpDemoScreen(
            root = view,
            title = getString(DemoR.string.demo_app_title),
            menu = R.menu.menu_catalog,
            content = list,
            showBack = false,
        )
    }
}
