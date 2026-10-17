package com.ts.ds.views.components.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.logic.TSDialogPresenter
import com.ts.ds.core.style.TSDialogVariant
import com.ts.ds.views.components.toast.TSToastHost

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

    inline fun <T> claimAround(owner: Any, refused: T, operation: () -> T): T {
        if (!presenter.begin(owner)) return refused
        return try {
            operation()
        } catch (error: Throwable) {
            presenter.end(owner)
            throw error
        }
    }

    fun claim(owner: Any, savedInstanceState: Bundle?): Boolean = savedInstanceState == null && presenter.begin(owner)

    fun drop(fragment: DialogFragment, savedInstanceState: Bundle?) {
        fragment.showsDialog = false
        fragment.dismissAllowingStateLoss()
        if (savedInstanceState == null) return
        fragment.parentFragmentManager.setFragmentResult(
            TSDialogFragmentResults.DROPPED,
            bundleOf(TSDialogFragmentResults.KEY_TAG to fragment.tag),
        )
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
}
