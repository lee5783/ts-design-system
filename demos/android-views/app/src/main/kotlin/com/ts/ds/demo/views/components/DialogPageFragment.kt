package com.ts.ds.demo.views.components

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.logic.TSDialogPresenter
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSDialogVariant
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.sample.SampleData
import com.ts.ds.demo.views.R
import com.ts.ds.demo.views.shell.DemoPageFragment
import com.ts.ds.views.components.button.TSButtonView
import com.ts.ds.views.components.dialog.TSBottomSheetDialogFragment
import com.ts.ds.views.components.dialog.TSDialogFragment
import com.ts.ds.views.components.textfield.TSTextFieldView

class DialogPageFragment : DemoPageFragment(R.layout.page_dialog) {
    override val page: DemoPageId = DemoPageId.Dialog

    private var playgroundVariant = TSDialogVariant.Center
    private var playgroundLong = false
    private var refusedCaption: TextView? = null
    private var projectName by pageState.string()
    private var renameField: TSTextFieldView? = null
    private var renameDraft: String? = null
    private val presenters = mutableMapOf<String, () -> Unit>()
    private val refusedListener: (TSDialogPresenter) -> Unit = { updateRefused(it.refusedCount) }

    private val confirm = SampleData.message("confirm-delete")

    override fun bindRows(rows: LinearLayout) {
        bindVariants(rows.findViewById(R.id.dialog_variants))
        bindContents(rows.findViewById(R.id.dialog_contents))
        bindStacking(rows.findViewById(R.id.dialog_stacking))
        bindChaining(rows.findViewById(R.id.dialog_chaining))
        bindPlayground(rows.findViewById(R.id.dialog_playground))
    }

    override fun onDestroyView() {
        TSDialogPresenter.shared.removeListener(refusedListener)
        refusedCaption = null
        renameField = null
        presenters.clear()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        renameField?.let { outState.putString(RENAME_DRAFT, it.text.toString()) }
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        renameDraft = savedInstanceState?.getString(RENAME_DRAFT)
    }

    override fun representDialog(tag: String) {
        presenters[tag]?.invoke()
    }

    private fun presenter(tag: String, present: () -> Unit): () -> Unit = present.also { presenters[tag] = it }

    private fun button(label: Int, tag: String, variant: TSButtonVariant = TSButtonVariant.Secondary, onClick: () -> Unit): TSButtonView =
        TSButtonView(requireContext()).apply {
            text = getString(label)
            this.variant = variant
            this.tag = tag
            setOnClickListener { onClick() }
        }

    private fun resultCaption(section: LinearLayout, tag: String? = null): TextView =
        section.addCaption(getString(DemoR.string.demo_component_dialog_result, getString(DemoR.string.demo_component_none))).also { it.tag = tag }

    private fun TextView.showResult(result: String) {
        text = getString(DemoR.string.demo_component_dialog_result, result)
    }

    private fun deleteActions(caption: TextView?): List<TSDialogAction> {
        val cancel = getString(DemoR.string.demo_sample_cancel)
        val delete = getString(DemoR.string.demo_sample_delete)
        return listOf(
            TSDialogAction(cancel) { caption?.showResult(cancel) },
            TSDialogAction(delete, TSButtonVariant.Danger) { caption?.showResult(delete) },
        )
    }

    private fun centerDialog(title: CharSequence, message: CharSequence?, actions: List<TSDialogAction>, onDismiss: (() -> Unit)? = null): TSDialogFragment =
        TSDialogFragment().apply {
            dialogTitle = title
            this.message = message
            this.actions = actions
            onUserDismiss = onDismiss
        }

    private fun sheetDialog(title: CharSequence, message: CharSequence?, actions: List<TSDialogAction>, onDismiss: (() -> Unit)? = null): TSBottomSheetDialogFragment =
        TSBottomSheetDialogFragment().apply {
            dialogTitle = title
            this.message = message
            this.actions = actions
            onUserDismiss = onDismiss
        }

    private fun bindVariants(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_variants)
        val row = section.addFlowRow()
        lateinit var caption: TextView
        val dismissed = { caption.showResult(getString(DemoR.string.demo_component_dismissed)) }
        val openCenter = presenter(CENTER_TAG) {
            centerDialog(getString(confirm.titleRes), getString(confirm.bodyRes), deleteActions(caption), dismissed)
                .present(childFragmentManager, CENTER_TAG)
        }
        val openSheet = presenter(SHEET_TAG) {
            sheetDialog(getString(confirm.titleRes), getString(confirm.bodyRes), deleteActions(caption), dismissed)
                .present(childFragmentManager, SHEET_TAG)
        }
        row.addView(button(DemoR.string.demo_component_open_dialog, OPEN_CENTER) { openCenter() })
        row.addView(button(DemoR.string.demo_component_open_sheet, OPEN_SHEET) { openSheet() })
        caption = resultCaption(section, RESULT)
    }

    private fun bindContents(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_contents)
        lateinit var caption: TextView
        val openInput = presenter(INPUT_TAG) {
            val field = TSTextFieldView(requireContext()).apply {
                label = getString(DemoR.string.demo_sample_project_name)
                text = renameDraft ?: currentProjectName()
                tag = INPUT_FIELD
            }
            renameDraft = null
            val title = getString(DemoR.string.demo_sample_rename_title)
            val cancel = getString(DemoR.string.demo_sample_cancel)
            val rename = getString(DemoR.string.demo_sample_rename)
            val dismissed = getString(DemoR.string.demo_component_dismissed)
            val closed = { result: String ->
                renameField = null
                caption.showResult(result)
            }
            renameField = field
            TSDialogFragment().apply {
                dialogTitle = title
                bodyView = field
                actions = listOf(
                    TSDialogAction(cancel) { closed(cancel) },
                    TSDialogAction(rename, TSButtonVariant.Primary) {
                        projectName = field.text.toString()
                        closed(currentProjectName())
                    },
                )
                onUserDismiss = { closed(dismissed) }
            }.present(childFragmentManager, INPUT_TAG)
        }
        section.addSpaced(button(DemoR.string.demo_component_with_input, OPEN_INPUT) { openInput() })
        caption = resultCaption(section)
    }

    private fun currentProjectName(): String = projectName ?: getString(DemoR.string.demo_sample_project_name_value)

    private fun bindStacking(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_stacking)
        val openStacking = presenter(STACKING_TAG) {
            centerDialog(
                getString(DemoR.string.demo_sample_stacking_title),
                getString(DemoR.string.demo_sample_stacking_body),
                listOf(
                    TSDialogAction(getString(DemoR.string.demo_sample_close)),
                    TSDialogAction(getString(DemoR.string.demo_component_open_another), TSButtonVariant.Primary, dismisses = false) {
                        centerDialog(getString(confirm.titleRes), getString(confirm.bodyRes), deleteActions(null))
                            .present(childFragmentManager, SECOND_DIALOG_TAG)
                    },
                ),
            ).present(childFragmentManager, STACKING_TAG)
        }
        section.addSpaced(button(DemoR.string.demo_component_stacking, OPEN_STACKING) { openStacking() })
        refusedCaption = section.addCaption("").also { it.tag = REFUSED }
        val presenter = TSDialogPresenter.shared
        presenter.addListener(refusedListener)
        updateRefused(presenter.refusedCount)
    }

    private fun bindChaining(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_chaining)
        lateinit var caption: TextView
        val cancel = getString(DemoR.string.demo_sample_cancel)
        val close = getString(DemoR.string.demo_sample_close)
        val undo = getString(DemoR.string.demo_sample_undo)
        val openDeleted = presenter(CHAIN_DELETED_TAG) {
            centerDialog(
                getString(DemoR.string.demo_sample_project_deleted_title),
                getString(DemoR.string.demo_sample_project_deleted_body),
                listOf(
                    TSDialogAction(close) { caption.showResult(close) },
                    TSDialogAction(undo, TSButtonVariant.Primary) { caption.showResult(undo) },
                ),
                onDismiss = { caption.showResult(getString(DemoR.string.demo_component_dismissed)) },
            ).present(childFragmentManager, CHAIN_DELETED_TAG)
        }
        val openConfirm = presenter(CHAIN_CONFIRM_TAG) {
            centerDialog(
                getString(confirm.titleRes),
                getString(confirm.bodyRes),
                listOf(
                    TSDialogAction(cancel) { caption.showResult(cancel) },
                    TSDialogAction(getString(DemoR.string.demo_sample_delete), TSButtonVariant.Danger) { openDeleted() },
                ),
                onDismiss = { caption.showResult(getString(DemoR.string.demo_component_dismissed)) },
            ).present(childFragmentManager, CHAIN_CONFIRM_TAG)
        }
        section.addSpaced(button(DemoR.string.demo_component_delete_with_undo, OPEN_CHAIN) { openConfirm() })
        caption = resultCaption(section, CHAIN_RESULT)
    }

    private fun updateRefused(count: Int) {
        refusedCaption?.text = getString(DemoR.string.demo_component_refused_count, count)
    }

    private fun bindPlayground(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_playground)
        lateinit var caption: TextView
        section.addCaption(getString(DemoR.string.demo_component_variant))
        section.addChoiceGroup(
            TSDialogVariant.entries,
            { getString(if (it == TSDialogVariant.Center) DemoR.string.demo_component_center else DemoR.string.demo_component_sheet) },
            playgroundVariant,
            { "demo.dialog.playground.variant.${it.name.lowercase()}" },
        ) { playgroundVariant = it }
        section.addToggle(DemoR.string.demo_component_long_content, tag = PLAYGROUND_LONG) { playgroundLong = it }
        val openPlayground = presenter(PLAYGROUND_TAG) {
            val title = getString(if (playgroundLong) DemoR.string.demo_sample_archive_title else confirm.titleRes)
            val message = getString(if (playgroundLong) DemoR.string.demo_sample_long_dialog_body else confirm.bodyRes)
            val cancel = getString(DemoR.string.demo_sample_cancel)
            val confirmLabel = getString(if (playgroundLong) DemoR.string.demo_sample_archive else DemoR.string.demo_sample_delete)
            val actions = listOf(
                TSDialogAction(cancel) { caption.showResult(cancel) },
                TSDialogAction(confirmLabel, if (playgroundLong) TSButtonVariant.Primary else TSButtonVariant.Danger) { caption.showResult(confirmLabel) },
            )
            val dismissed = { caption.showResult(getString(DemoR.string.demo_component_dismissed)) }
            when (playgroundVariant) {
                TSDialogVariant.Center -> centerDialog(title, message, actions, dismissed).present(childFragmentManager, PLAYGROUND_TAG)
                TSDialogVariant.Sheet -> sheetDialog(title, message, actions, dismissed).present(childFragmentManager, PLAYGROUND_TAG)
            }
        }
        section.addSpaced(button(DemoR.string.demo_component_open_dialog, PLAYGROUND_OPEN, TSButtonVariant.Primary) { openPlayground() })
        caption = resultCaption(section)
    }

    companion object {
        const val OPEN_CENTER = "demo.dialog.open.center"
        const val OPEN_SHEET = "demo.dialog.open.sheet"
        const val RESULT = "demo.dialog.result"
        const val OPEN_INPUT = "demo.dialog.open.input"
        const val INPUT_FIELD = "demo.dialog.input.field"
        const val OPEN_STACKING = "demo.dialog.open.stacking"
        const val REFUSED = "demo.dialog.refused"
        const val PLAYGROUND_LONG = "demo.dialog.playground.long"
        const val PLAYGROUND_OPEN = "demo.dialog.playground.open"
        const val OPEN_CHAIN = "demo.dialog.open.chain"
        const val CHAIN_RESULT = "demo.dialog.chain.result"
        const val CENTER_TAG = "demo.dialog.center"
        const val SHEET_TAG = "demo.dialog.sheet"
        const val INPUT_TAG = "demo.dialog.input"
        const val STACKING_TAG = "demo.dialog.stacking"
        const val SECOND_DIALOG_TAG = "demo.dialog.second"
        const val CHAIN_CONFIRM_TAG = "demo.dialog.chain.confirm"
        const val CHAIN_DELETED_TAG = "demo.dialog.chain.deleted"
        const val PLAYGROUND_TAG = "demo.dialog.playground"
        private const val RENAME_DRAFT = "demo.dialog.rename.draft"
    }
}
