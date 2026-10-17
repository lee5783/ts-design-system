package com.ts.ds.core.logic

import java.lang.ref.WeakReference

public class TSDialogPresenter {
    private val listeners = mutableListOf<(TSDialogPresenter) -> Unit>()
    private var ownerReference: WeakReference<Any>? = null

    private val activeOwner: Any?
        get() = ownerReference?.get()

    public var refusedCount: Int = 0
        private set

    public val isPresenting: Boolean
        get() = activeOwner != null

    public fun isActive(owner: Any): Boolean = activeOwner === owner

    public fun begin(owner: Any): Boolean {
        val current = activeOwner
        if (current != null && current !== owner) {
            refusedCount += 1
            notifyListeners()
            return false
        }
        if (current == null) {
            ownerReference = WeakReference(owner)
            notifyListeners()
        }
        return true
    }

    public fun end(owner: Any) {
        if (activeOwner !== owner) return
        ownerReference = null
        notifyListeners()
    }

    public fun addListener(listener: (TSDialogPresenter) -> Unit) {
        listeners += listener
    }

    public fun removeListener(listener: (TSDialogPresenter) -> Unit) {
        listeners -= listener
    }

    private fun notifyListeners() {
        listeners.toList().forEach { it(this) }
    }

    public companion object {
        public val shared: TSDialogPresenter = TSDialogPresenter()
    }
}
