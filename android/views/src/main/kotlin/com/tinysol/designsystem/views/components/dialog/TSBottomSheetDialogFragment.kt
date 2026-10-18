package com.tinysol.designsystem.views.components.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tinysol.designsystem.core.logic.TSDialogAction
import com.tinysol.designsystem.core.logic.TSDialogPresenter
import com.tinysol.designsystem.core.style.TSColorRole
import com.tinysol.designsystem.core.style.TSDialogVariant
import com.tinysol.designsystem.views.foundation.applySystemBarInsetsAsPadding
import com.tinysol.designsystem.views.theme.tsColor

public open class TSBottomSheetDialogFragment : BottomSheetDialogFragment() {
    internal val session = TSDialogSession(TSDialogVariant.Sheet)

    public var dialogTitle: CharSequence by session::title
    public var message: CharSequence? by session::message
    public var bodyView: View? by session::bodyView
    public var actions: List<TSDialogAction> by session::actions
    public var onUserDismiss: (() -> Unit)? by session::onUserDismiss
    public var presenter: TSDialogPresenter by session::presenter

    internal val panel: TSDialogPanelView?
        get() = session.panel

    public fun present(manager: FragmentManager, tag: String? = null): Boolean {
        if (!session.canCommit(manager)) return false
        return session.claimAround(this, false) {
            super.show(manager, tag)
            true
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        present(manager, tag)
    }

    override fun show(transaction: FragmentTransaction, tag: String?): Int =
        session.claimAround(this, -1) { super.show(transaction, tag) }

    override fun showNow(manager: FragmentManager, tag: String?) {
        session.claimAround(this, Unit) { super.showNow(manager, tag) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!session.claim(this, savedInstanceState)) session.drop(this, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            setTitle(dialogTitle)
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        session.makePanel(requireContext(), ::perform, ::cancelByUser).apply {
            applySystemBarInsetsAsPadding(start = true, end = true, bottom = true)
        }

    override fun onStart() {
        session.inheritSystemBars(this)
        super.onStart()
        val scrim = requireContext().tsColor(TSColorRole.Overlay)
        dialog?.window?.setDimAmount(Color.alpha(scrim) / 255f)
        session.attachToasts(requireContext(), dialog?.window)
    }

    override fun onStop() {
        session.detachToasts(requireContext())
        super.onStop()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onUserDismiss?.invoke()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        session.restoreHostSystemBars(this)
        presenter.end(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.end(this)
    }

    private fun perform(action: TSDialogAction) {
        session.perform(this, action)
    }

    private fun cancelByUser() {
        session.restoreHostSystemBars(this)
        dialog?.cancel()
    }
}
