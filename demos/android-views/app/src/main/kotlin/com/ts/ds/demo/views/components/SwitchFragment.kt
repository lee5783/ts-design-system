package com.ts.ds.demo.views.components

import android.widget.LinearLayout
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.views.components.switchcontrol.TSSwitchView

class SwitchFragment : DemoPageFragment(R.layout.page_switch) {
    override val page: DemoPageId = DemoPageId.Switch

    override fun bindRows(rows: LinearLayout) {
        rows.markSectionHeadings()
        rows.findViewById<LinearLayout>(R.id.toggle_states).bindToggleStates(SPEC)
        rows.findViewById<LinearLayout>(R.id.toggle_playground).bindTogglePlayground(SPEC)
    }

    companion object {
        val SPEC = DemoToggleViewSpec(
            page = "switch",
            stateSampleRes = DemoR.string.demo_sample_wifi,
            playgroundSampleRes = DemoR.string.demo_sample_dark_mode,
            onRes = DemoR.string.demo_state_on,
            offRes = DemoR.string.demo_state_off,
            create = ::TSSwitchView,
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
