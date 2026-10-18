package com.tinysol.designsystem.views.sample.shell

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tinysol.designsystem.core.style.TSListGroupPosition
import com.tinysol.designsystem.sample.support.catalog.DemoCatalog
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.sample.support.catalog.DemoSectionId
import com.tinysol.designsystem.views.sample.R
import com.tinysol.designsystem.views.components.listitem.TSListItemView

sealed interface CatalogItem {
    data class Header(val section: DemoSectionId) : CatalogItem

    data class Row(val page: DemoPageId, val position: TSListGroupPosition) : CatalogItem

    companion object {
        val all: List<CatalogItem> = DemoCatalog.sections.flatMap { section ->
            listOf(Header(section.id)) + section.pages.mapIndexed { index, page ->
                Row(page, TSListGroupPosition.make(index, section.pages.size))
            }
        }

        fun groupPosition(adapterPosition: Int): TSListGroupPosition? = (all.getOrNull(adapterPosition) as? Row)?.position
    }
}

class CatalogAdapter(
    private val onOpenPage: (DemoPageId) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = CatalogItem.all

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is CatalogItem.Header -> R.layout.item_catalog_header
        is CatalogItem.Row -> R.layout.item_catalog_row
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.item_catalog_header -> HeaderHolder(view)
            else -> RowHolder(view, onOpenPage)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CatalogItem.Header -> (holder as HeaderHolder).bind(item.section)
            is CatalogItem.Row -> (holder as RowHolder).bind(item.page)
        }
    }

    private class HeaderHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title = view as TextView

        fun bind(section: DemoSectionId) {
            title.setText(section.titleRes)
        }
    }

    private class RowHolder(
        view: View,
        private val onOpenPage: (DemoPageId) -> Unit,
    ) : RecyclerView.ViewHolder(view) {
        private val row = view as TSListItemView

        fun bind(page: DemoPageId) {
            row.title = row.context.getString(page.titleRes)
            row.tag = page.testTag
            row.setOnClickListener { onOpenPage(page) }
        }
    }
}
