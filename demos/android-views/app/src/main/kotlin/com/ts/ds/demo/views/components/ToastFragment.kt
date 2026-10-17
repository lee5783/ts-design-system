package com.ts.ds.demo.views.components

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.logic.TSToastTone
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.sample.SampleData
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.views.components.button.TSButtonView
import com.ts.ds.views.components.dialog.TSBottomSheetDialogFragment
import com.ts.ds.views.components.toast.TSToastHost
import com.ts.ds.views.components.toast.TSToastView

class ToastFragment : DemoPageFragment(R.layout.page_toast) {
    override val page: DemoPageId = DemoPageId.Toast

    private var playgroundTone = TSToastTone.Neutral
    private var playgroundAction = true
    private var openSheet: (() -> Unit)? = null

    private val TSToastTone.labelRes: Int
        get() = when (this) {
            TSToastTone.Neutral -> DemoR.string.demo_sample_neutral
            TSToastTone.Info -> DemoR.string.demo_sample_information
            TSToastTone.Success -> DemoR.string.demo_sample_success
            TSToastTone.Warning -> DemoR.string.demo_sample_warning
            TSToastTone.Danger -> DemoR.string.demo_sample_error
        }

    private val toastHost: TSToastHost
        get() = requireNotNull(TSToastHost.from(requireActivity()))

    override fun bindRows(rows: LinearLayout) {
        bindTones(rows.findViewById(R.id.toast_tones))
        bindContents(rows.findViewById(R.id.toast_contents))
        bindQueue(rows.findViewById(R.id.toast_queue))
        bindSheet(rows.findViewById(R.id.toast_sheet))
        bindPlayground(rows.findViewById(R.id.toast_playground))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) session.toastUndoTaps.value = 0
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        openSheet = null
        super.onDestroyView()
    }

    override fun representDialog(tag: String) {
        if (tag == SHEET_TAG) openSheet?.invoke()
    }

    private fun toast(message: CharSequence, configure: TSToastView.() -> Unit = {}): TSToastView =
        TSToastView(requireContext()).apply {
            this.message = message
            configure()
        }

    private fun button(label: Int, tag: String, variant: TSButtonVariant = TSButtonVariant.Secondary, onClick: () -> Unit): TSButtonView =
        TSButtonView(requireContext()).apply {
            text = getString(label)
            this.variant = variant
            this.tag = tag
            setOnClickListener { onClick() }
        }

    private fun bindTones(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_tones)
        val saved = SampleData.message("saved")
        section.addSpaced(toast(getString(DemoR.string.demo_sample_project_archived)))
        section.addSpaced(toast(getString(saved.titleRes)) { tone = TSToastTone.Success })
        section.addSpaced(toast(getString(DemoR.string.demo_sample_storage_title)) { tone = TSToastTone.Warning })
        section.addSpaced(toast(getString(DemoR.string.demo_sample_payment_title)) { tone = TSToastTone.Danger })
    }

    private fun bindContents(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_contents)
        section.addSpaced(
            toast(getString(DemoR.string.demo_sample_project_archived)) {
                actionLabel = getString(DemoR.string.demo_sample_undo)
                onAction = {}
            },
        )
        section.addSpaced(
            toast(getString(DemoR.string.demo_sample_long_toast)) {
                tone = TSToastTone.Info
                actionLabel = getString(DemoR.string.demo_sample_open)
                onAction = {}
            },
        )
    }

    private fun bindQueue(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_queue)
        section.addSpaced(
            button(DemoR.string.demo_component_show_three_toasts, QUEUE_SHOW) {
                listOf(
                    DemoR.string.demo_sample_photo_uploaded,
                    DemoR.string.demo_sample_album_shared,
                    DemoR.string.demo_sample_backup_complete,
                ).forEach { toastHost.show(getString(it), TSToastTone.Success) }
            },
        )
    }

    private fun bindSheet(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_over_sheet)
        val present: () -> Unit = {
            val title = getString(DemoR.string.demo_sample_toast_sheet_title)
            val body = getString(DemoR.string.demo_sample_toast_sheet_body)
            val close = getString(DemoR.string.demo_sample_close)
            val copyLink = getString(DemoR.string.demo_sample_copy_link)
            val linkCopied = getString(DemoR.string.demo_sample_link_copied)
            TSBottomSheetDialogFragment().apply {
                dialogTitle = title
                message = body
                actions = listOf(
                    TSDialogAction(close),
                    TSDialogAction(copyLink, TSButtonVariant.Primary, dismisses = false) {
                        toastHost.show(linkCopied, TSToastTone.Success)
                    },
                )
            }.present(childFragmentManager, SHEET_TAG)
        }
        openSheet = present
        section.addSpaced(button(DemoR.string.demo_component_open_sheet, SHEET_OPEN) { present() })
    }

    private fun bindPlayground(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_playground)
        section.addCaption(getString(DemoR.string.demo_component_tone))
        section.addChoiceGroup(TSToastTone.entries, { getString(it.labelRes) }, playgroundTone, { "demo.toast.playground.tone.${it.name.lowercase()}" }) {
            playgroundTone = it
        }
        section.addToggle(DemoR.string.demo_component_action, checked = true, tag = PLAYGROUND_ACTION) { playgroundAction = it }
        section.addSpaced(
            button(DemoR.string.demo_component_show_toast, PLAYGROUND_SHOW, TSButtonVariant.Primary) {
                val message = getString(SampleData.message("deleted").titleRes)
                if (playgroundAction) {
                    val undoTaps = session.toastUndoTaps
                    toastHost.show(message, playgroundTone, getString(DemoR.string.demo_sample_undo)) { undoTaps.value = (undoTaps.value ?: 0) + 1 }
                } else {
                    toastHost.show(message, playgroundTone)
                }
            },
        )
        val counter = section.addTapCounter(PLAYGROUND_COUNT)
        session.toastUndoTaps.observe(viewLifecycleOwner) { counter.count = it }
    }

    companion object {
        const val QUEUE_SHOW = "demo.toast.queue.show"
        const val SHEET_OPEN = "demo.toast.sheet.open"
        const val PLAYGROUND_ACTION = "demo.toast.playground.action"
        const val PLAYGROUND_SHOW = "demo.toast.playground.show"
        const val PLAYGROUND_COUNT = "demo.toast.playground.count"
        const val SHEET_TAG = "demo.toast.sheet"
    }
}
