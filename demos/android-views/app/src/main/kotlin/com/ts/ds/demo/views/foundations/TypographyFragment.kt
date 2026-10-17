package com.ts.ds.demo.views.foundations

import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.views.R as ViewsR

private const val SPEC_SEPARATOR = " · "

class TypographyFragment : DemoPageFragment(R.layout.page_typography) {
    override val page: DemoPageId = DemoPageId.Typography

    override fun bindRows(rows: LinearLayout) {
        val inflater = LayoutInflater.from(rows.context)
        TSTypeRole.entries.forEach { role ->
            val card = inflater.inflate(R.layout.item_type_sample, rows, false)
            val values = role.values
            val spec = listOf(role.name, "${values.fontSize.toInt()}/${values.lineHeight.toInt()}", values.fontWeight.toString())
            card.findViewById<TextView>(R.id.type_spec).text = spec.joinToString(SPEC_SEPARATOR)
            val sample = card.findViewById<TextView>(R.id.type_sample)
            TextViewCompat.setTextAppearance(sample, role.textAppearance)
            sample.isAllCaps = role == TSTypeRole.Overline
            rows.addView(card)
        }
    }
}

private val TSTypeRole.textAppearance: Int
    get() = when (this) {
        TSTypeRole.Display -> ViewsR.style.TextAppearance_TS_Display
        TSTypeRole.Title1 -> ViewsR.style.TextAppearance_TS_Title1
        TSTypeRole.Title2 -> ViewsR.style.TextAppearance_TS_Title2
        TSTypeRole.Title3 -> ViewsR.style.TextAppearance_TS_Title3
        TSTypeRole.BodyLg -> ViewsR.style.TextAppearance_TS_BodyLg
        TSTypeRole.Body -> ViewsR.style.TextAppearance_TS_Body
        TSTypeRole.BodySm -> ViewsR.style.TextAppearance_TS_BodySm
        TSTypeRole.Label -> ViewsR.style.TextAppearance_TS_Label
        TSTypeRole.Caption -> ViewsR.style.TextAppearance_TS_Caption
        TSTypeRole.Overline -> ViewsR.style.TextAppearance_TS_Overline
        TSTypeRole.Code -> ViewsR.style.TextAppearance_TS_Code
    }
