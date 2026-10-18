package com.tinysol.designsystem.views.sample.components

import android.widget.LinearLayout
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.views.sample.R
import com.tinysol.designsystem.views.sample.shell.DemoPageFragment
import com.tinysol.designsystem.views.components.checkbox.TSCheckboxView

class CheckboxFragment : DemoPageFragment(R.layout.page_checkbox) {
    override val page: DemoPageId = DemoPageId.Checkbox

    override fun bindRows(rows: LinearLayout) {
        rows.findViewById<LinearLayout>(R.id.toggle_states).bindToggleStates(SPEC)
        rows.findViewById<LinearLayout>(R.id.toggle_playground).bindTogglePlayground(SPEC)
    }

    companion object {
        val SPEC = DemoToggleViewSpec(
            page = "checkbox",
            stateSampleRes = DemoR.string.demo_sample_email_updates,
            playgroundSampleRes = DemoR.string.demo_sample_accept_terms,
            onRes = DemoR.string.demo_component_checked,
            offRes = DemoR.string.demo_component_unchecked,
            create = ::TSCheckboxView,
            bind = { label, checked, labelHidden ->
                this.label = label
                isChecked = checked
                isLabelHidden = labelHidden
            },
            setLabelHidden = { isLabelHidden = it },
            onChange = { listener -> setOnCheckedChangeListener { _, checked -> listener(checked) } },
        )
    }
}
