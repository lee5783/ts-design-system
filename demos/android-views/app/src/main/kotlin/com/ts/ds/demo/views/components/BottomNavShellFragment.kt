package com.ts.ds.demo.views.components

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ts.ds.core.logic.TSToastTone
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.setUpDemoToolbar
import com.ts.ds.views.components.bottomnav.TSBottomNavView
import com.ts.ds.views.components.button.TSButtonView
import com.ts.ds.views.components.toast.TSToastHost
import com.ts.ds.views.foundation.applySystemBarInsetsAsPadding
import com.ts.ds.views.theme.textAppearance
import com.ts.ds.views.theme.tsColor

class BottomNavShellFragment : Fragment(R.layout.page_bottom_nav_shell) {
    private var selection = "home"
    private var inboxBadge = 3
    private var nav: TSBottomNavView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.tag = SHELL
        savedInstanceState?.let {
            selection = it.getString(KEY_SELECTION, selection)
            inboxBadge = it.getInt(KEY_INBOX_BADGE, inboxBadge)
        }
        setUpDemoToolbar(view.findViewById(R.id.demo_toolbar), getString(DemoR.string.demo_component_shell), R.menu.menu_page)
        view.findViewById<View>(R.id.shell_scroll).applySystemBarInsetsAsPadding(start = true, end = true)
        val content = view.findViewById<LinearLayout>(R.id.shell_content)
        val bar = view.findViewById<TSBottomNavView>(R.id.shell_nav)
        nav = bar
        bar.setOnItemSelectedListener { _, id ->
            selection = id
            render(content)
        }
        render(content)
        TSToastHost.from(requireActivity())?.anchorView = bar
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SELECTION, selection)
        outState.putInt(KEY_INBOX_BADGE, inboxBadge)
    }

    override fun onDestroyView() {
        val host = TSToastHost.from(requireActivity())
        if (host?.anchorView === nav) host?.anchorView = null
        nav = null
        super.onDestroyView()
    }

    private fun render(content: LinearLayout) {
        val bar = nav ?: return
        bar.items = requireContext().navItems(4, inboxBadge = inboxBadge)
        bar.selectedId = selection
        val item = bar.items.first { it.id == bar.selectedId }
        content.removeAllViews()
        content.addSpaced(
            TextView(content.context).apply {
                setTextAppearance(TSTypeRole.Title2.textAppearance)
                setTextColor(context.tsColor(TSColorRole.TextPrimary))
                textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                text = item.label
                tag = "demo.bottom-nav.shell.title.${item.id}"
            },
            matchWidth = true,
        )
        content.addSpaced(
            TextView(content.context).apply {
                setTextAppearance(TSTypeRole.Body.textAppearance)
                setTextColor(context.tsColor(TSColorRole.TextSecondary))
                textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                setText(DemoR.string.demo_sample_shell_body)
            },
            matchWidth = true,
        )
        content.addSpaced(button(DemoR.string.demo_sample_new_message, "demo.bottom-nav.shell.add-one.${item.id}") {
            inboxBadge += 1
            render(content)
        })
        content.addSpaced(button(DemoR.string.demo_sample_mark_all_read, "demo.bottom-nav.shell.clear.${item.id}", TSButtonVariant.Ghost) {
            inboxBadge = 0
            render(content)
        })
        content.addSpaced(button(DemoR.string.demo_component_show_toast, "demo.bottom-nav.shell.toast.${item.id}") {
            TSToastHost.from(requireActivity())?.show(getString(DemoR.string.demo_sample_message_sent), TSToastTone.Success)
        })
    }

    private fun button(label: Int, tag: String, variant: TSButtonVariant = TSButtonVariant.Secondary, onClick: () -> Unit): TSButtonView =
        TSButtonView(requireContext()).apply {
            text = getString(label)
            this.variant = variant
            this.tag = tag
            setOnClickListener { onClick() }
        }

    companion object {
        const val SHELL = "demo.bottom-nav.shell"
        private const val KEY_SELECTION = "demo.bottom-nav.shell.selection"
        private const val KEY_INBOX_BADGE = "demo.bottom-nav.shell.inbox-badge"
    }
}
