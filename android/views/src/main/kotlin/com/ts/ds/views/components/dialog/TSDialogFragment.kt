package com.ts.ds.views.components.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.logic.TSDialogPresenter
import com.ts.ds.core.style.TSDialogStyleSpec
import com.ts.ds.core.style.TSDialogVariant
import com.ts.ds.views.R

public open class TSDialogFragment : AppCompatDialogFragment() {
    internal val session = TSDialogSession(TSDialogVariant.Center)

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
        AppCompatDialog(requireContext(), R.style.ThemeOverlay_TS_CenterDialog).apply {
            setTitle(dialogTitle)
            setCanceledOnTouchOutside(false)
            window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
            onBackPressedDispatcher.addCallback(this, PredictiveBackCallback())
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val panel = session.makePanel(requireContext(), ::perform, ::cancelByUser)
        return TSDialogScrimLayout(requireContext()).apply {
            this.panel = panel
            onScrimTap = ::cancelByUser
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
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
        presenter.end(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.end(this)
    }

    private fun perform(action: TSDialogAction) {
        if (action.dismisses) dismiss()
        action.onClick()
    }

    private fun cancelByUser() {
        dialog?.cancel()
    }

    private fun applyBackScale(scale: Float) {
        panel?.apply {
            scaleX = scale
            scaleY = scale
        }
    }

    private inner class PredictiveBackCallback : OnBackPressedCallback(true) {
        override fun handleOnBackProgressed(backEvent: BackEventCompat) {
            applyBackScale(TSDialogStyleSpec.predictiveBackScale(backEvent.progress))
        }

        override fun handleOnBackCancelled() {
            applyBackScale(1f)
        }

        override fun handleOnBackPressed() {
            cancelByUser()
        }
    }
}
