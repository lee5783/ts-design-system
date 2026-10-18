package com.tinysol.designsystem.views.components.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.tinysol.designsystem.core.logic.TSDialogAction
import com.tinysol.designsystem.core.logic.TSDialogPresenter
import com.tinysol.designsystem.core.style.TSDialogVariant
import com.tinysol.designsystem.views.components.toast.TSToastHost
import java.lang.ref.WeakReference

internal class TSDialogSession(private val variant: TSDialogVariant) {
    var title: CharSequence = ""
    var message: CharSequence? = null
    var bodyView: View? = null
    var actions: List<TSDialogAction> = emptyList()
    var onUserDismiss: (() -> Unit)? = null
    var presenter: TSDialogPresenter = TSDialogPresenter.shared
    var panel: TSDialogPanelView? = null
        private set

    private var toastContainer: ViewGroup? = null
    private var hostBars: TSSystemBarsState? = null

    fun attachToasts(context: Context, window: Window?) {
        val container = window?.decorView as? ViewGroup ?: return
        TSToastHost.from(context)?.attachOverlay(container)
        toastContainer = container
    }

    fun detachToasts(context: Context) {
        val container = toastContainer ?: return
        TSToastHost.from(context)?.detachOverlay(container)
        toastContainer = null
    }

    fun canCommit(manager: FragmentManager): Boolean = !manager.isStateSaved && !manager.isDestroyed

    fun begin(owner: Any): Boolean {
        val chaining = chainingOwner?.get()
        if (chaining != null && chaining !== owner && presenter.isActive(chaining)) return presenter.handOver(chaining, owner)
        return presenter.begin(owner)
    }

    inline fun <T> claimAround(owner: Any, refused: T, operation: () -> T): T {
        if (!begin(owner)) return refused
        return try {
            operation()
        } catch (error: Throwable) {
            presenter.end(owner)
            throw error
        }
    }

    fun claim(owner: Any, savedInstanceState: Bundle?): Boolean = savedInstanceState == null && begin(owner)

    fun drop(fragment: DialogFragment, savedInstanceState: Bundle?) {
        fragment.showsDialog = false
        fragment.dismissAllowingStateLoss()
        if (savedInstanceState == null) return
        fragment.parentFragmentManager.setFragmentResult(
            TSDialogFragmentResults.DROPPED,
            bundleOf(TSDialogFragmentResults.KEY_TAG to fragment.tag),
        )
    }

    fun perform(owner: DialogFragment, action: TSDialogAction) {
        if (!action.dismisses) {
            action.onClick()
            return
        }
        chainingOwner = WeakReference(owner)
        try {
            action.onClick()
        } finally {
            chainingOwner = null
        }
        restoreHostSystemBars(owner)
        if (owner.isAdded) owner.dismissAllowingStateLoss() else presenter.end(owner)
    }

    fun inheritSystemBars(fragment: DialogFragment) {
        val host = fragment.activity?.window ?: return
        val state = hostBars ?: host.systemBarsState().also { hostBars = it }
        fragment.dialog?.window?.applySystemBars(state)
    }

    fun restoreHostSystemBars(fragment: DialogFragment) {
        val state = hostBars ?: return
        fragment.activity?.window?.applySystemBars(state)
    }

    fun makePanel(context: Context, onAction: (TSDialogAction) -> Unit, onClose: () -> Unit): TSDialogPanelView =
        TSDialogPanelView(context).also {
            it.variant = variant
            it.title = title
            it.message = message
            it.bodyView = bodyView
            it.actions = actions
            it.onAction = onAction
            it.onClose = onClose
            panel = it
        }

    private companion object {
        var chainingOwner: WeakReference<Any>? = null
    }
}
