package com.ts.ds.demo.views.components

import android.widget.LinearLayout
import androidx.core.widget.doAfterTextChanged
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.input.TSTextInputTraits
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.views.components.iconbutton.TSIconButtonView
import com.ts.ds.views.components.textfield.TSTextFieldView

class TextFieldFragment : DemoPageFragment(R.layout.page_text_field) {
    override val page: DemoPageId = DemoPageId.TextField

    override fun bindRows(rows: LinearLayout) {
        bindWithIcons(rows.findViewById(R.id.text_field_with_icons))
        bindPlayground(rows.findViewById(R.id.text_field_playground))
    }

    private fun bindWithIcons(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_with_icons)
        val field = TSTextFieldView(section.context).apply {
            label = getString(DemoR.string.demo_sample_search)
            placeholder = getString(DemoR.string.demo_sample_search_placeholder)
            leadingIcon = TSIcon.Search
            inputTraits = TSTextInputTraits.Search
            text = getString(R.string.demo_text_field_sample_search)
        }
        val clear = TSIconButtonView(section.context).apply {
            icon = TSIcon.Close
            label = getString(DemoR.string.demo_sample_clear)
            setOnClickListener { field.text = "" }
        }
        field.trailingView = clear
        field.editText.doAfterTextChanged { field.trailingView = if (it.isNullOrEmpty()) null else clear }
        section.addSpaced(field, matchWidth = true)
    }

    private fun bindPlayground(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_playground)
        val field = TSTextFieldView(section.context).apply {
            tag = PLAYGROUND_FIELD
            label = getString(DemoR.string.demo_sample_email)
            placeholder = getString(DemoR.string.demo_sample_email_placeholder)
            hint = getString(DemoR.string.demo_sample_email_hint)
            inputTraits = TSTextInputTraits.Email
        }
        section.addToggle(DemoR.string.demo_component_error, tag = PLAYGROUND_ERROR) {
            field.errorMessage = if (it) getString(DemoR.string.demo_sample_email_error) else null
        }
        section.addToggle(DemoR.string.demo_component_disabled, tag = PLAYGROUND_DISABLED) { field.isEnabled = !it }
        section.addToggle(DemoR.string.demo_component_read_only, tag = PLAYGROUND_READ_ONLY) { field.isReadOnly = it }
        section.addToggle(DemoR.string.demo_component_leading_icon, tag = PLAYGROUND_LEADING_ICON) {
            field.leadingIcon = if (it) TSIcon.Mail else null
        }
        section.addToggle(DemoR.string.demo_component_secure, tag = PLAYGROUND_SECURE) { field.isSecure = it }
        section.addToggle(DemoR.string.demo_component_multiline, tag = PLAYGROUND_MULTILINE) { field.isMultiline = it }
        section.addSpaced(field, matchWidth = true)
    }

    companion object {
        const val PLAYGROUND_FIELD = "demo.text-field.playground.field"
        const val PLAYGROUND_ERROR = "demo.text-field.playground.error"
        const val PLAYGROUND_DISABLED = "demo.text-field.playground.disabled"
        const val PLAYGROUND_READ_ONLY = "demo.text-field.playground.read-only"
        const val PLAYGROUND_LEADING_ICON = "demo.text-field.playground.leading-icon"
        const val PLAYGROUND_SECURE = "demo.text-field.playground.secure"
        const val PLAYGROUND_MULTILINE = "demo.text-field.playground.multiline"
        const val CODE_FIELD = "demo.text-field.code.field"
    }
}
