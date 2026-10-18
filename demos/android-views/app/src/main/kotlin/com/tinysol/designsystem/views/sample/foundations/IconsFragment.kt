package com.tinysol.designsystem.views.sample.foundations

import android.content.res.ColorStateList
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.widget.ImageViewCompat
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.icon.dsName
import com.tinysol.designsystem.core.style.TSColorRole
import com.tinysol.designsystem.core.style.TSIconSize
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.views.sample.R
import com.tinysol.designsystem.views.sample.components.addCaption
import com.tinysol.designsystem.views.sample.components.addFlowRow
import com.tinysol.designsystem.views.sample.components.addSectionHeader
import com.tinysol.designsystem.views.sample.shell.DemoPageFragment
import com.tinysol.designsystem.views.sample.shell.addTokenRow
import com.tinysol.designsystem.views.sample.shell.dpToPx
import com.tinysol.designsystem.views.theme.tsColor

class IconsFragment : DemoPageFragment(R.layout.page_icons) {
    override val page: DemoPageId = DemoPageId.Icons

    override fun bindRows(rows: LinearLayout) {
        rows.addSectionHeader(DemoR.string.demo_component_sizes)
        TSIconSize.entries.forEach { size ->
            val row = rows.addFlowRow()
            listOf(TSIcon.Home, TSIcon.Search, TSIcon.ChevronRight, TSIcon.ArrowLeft).forEach { icon ->
                row.addView(icon(icon, size, TSColorRole.TextPrimary, null))
            }
            row.addCaption("${size.name.lowercase()} · ${size.dp.toInt()}dp")
        }
        rows.addSectionHeader(DemoR.string.demo_component_label)
        val labelled = rows.addFlowRow()
        listOf(
            Triple(TSIcon.CheckCircle, TSColorRole.Success, DemoR.string.demo_sample_success),
            Triple(TSIcon.Alert, TSColorRole.Warning, DemoR.string.demo_sample_warning),
            Triple(TSIcon.XCircle, TSColorRole.Danger, DemoR.string.demo_sample_error),
            Triple(TSIcon.Info, TSColorRole.Info, DemoR.string.demo_sample_information),
        ).forEach { (icon, role, label) -> labelled.addView(icon(icon, TSIconSize.Lg, role, getString(label))) }
        rows.addSectionHeader(DemoR.string.demo_icons_all)
        TSIcon.entries.forEach { icon ->
            val value = if (icon.autoMirrored) "${icon.dsName} · mirrored" else icon.dsName
            rows.addTokenRow(name = icon.name, value = value) { host ->
                host.addView(icon(icon, TSIconSize.Lg, TSColorRole.TextPrimary, null))
            }
        }
    }

    private fun icon(icon: TSIcon, size: TSIconSize, role: TSColorRole, label: String?): ImageView =
        ImageView(requireContext()).apply {
            setImageResource(icon.drawableRes)
            ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(tsColor(role)))
            contentDescription = label
            importantForAccessibility = if (label == null) View.IMPORTANT_FOR_ACCESSIBILITY_NO else View.IMPORTANT_FOR_ACCESSIBILITY_YES
            val px = requireContext().dpToPx(size.dp)
            layoutParams = FrameLayout.LayoutParams(px, px)
        }
}
