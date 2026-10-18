package com.ts.ds.core.logic

import java.lang.ref.WeakReference

public enum class TSDialogPolicy {
    Queue,
    Refuse,
}

public enum class TSDialogRequestResult {
    Granted,
    Queued,
    Refused,
}

public class TSDialogPresenter(
    public var policy: TSDialogPolicy = TSDialogPolicy.Queue,
) {
    private val listeners = mutableListOf<(TSDialogPresenter) -> Unit>()
    private val pending = ArrayDeque<PendingRequest>()
    private var ownerReference: WeakReference<Any>? = null

    private val activeOwner: Any?
        get() = ownerReference?.get()

    public var refusedCount: Int = 0
        private set

    public val isPresenting: Boolean
        get() = activeOwner != null

    public val pendingCount: Int
        get() = pending.count { it.owner.get() != null }

    public fun isActive(owner: Any): Boolean = activeOwner === owner

    public fun isPending(owner: Any): Boolean = pending.any { it.owner.get() === owner }

    public fun begin(owner: Any): Boolean {
        grantNext()
        val current = activeOwner
        if (current != null && current !== owner) {
            refuse()
            return false
        }
        if (current == null) {
            ownerReference = WeakReference(owner)
            notifyListeners()
        }
        return true
    }

    public fun request(owner: Any, onGranted: () -> Unit): TSDialogRequestResult {
        prunePending()
        grantNext()
        val current = activeOwner
        if (current == null || current === owner) {
            begin(owner)
            return TSDialogRequestResult.Granted
        }
        if (policy == TSDialogPolicy.Refuse) {
            refuse()
            return TSDialogRequestResult.Refused
        }
        val existing = pending.firstOrNull { it.owner.get() === owner }
        if (existing != null) {
            existing.onGranted = onGranted
        } else {
            pending.addLast(PendingRequest(WeakReference(owner), onGranted))
            notifyListeners()
        }
        return TSDialogRequestResult.Queued
    }

    public fun handOver(from: Any, to: Any): Boolean {
        if (activeOwner !== from) return false
        pending.removeAll { it.owner.get() === to }
        ownerReference = WeakReference(to)
        notifyListeners()
        return true
    }

    public fun end(owner: Any) {
        prunePending()
        if (activeOwner === owner) {
            ownerReference = null
            notifyListeners()
            grantNext()
            return
        }
        val index = pending.indexOfFirst { it.owner.get() === owner }
        if (index < 0) return
        pending.removeAt(index)
        notifyListeners()
    }

    public fun addListener(listener: (TSDialogPresenter) -> Unit) {
        listeners += listener
    }

    public fun removeListener(listener: (TSDialogPresenter) -> Unit) {
        listeners -= listener
    }

    private fun refuse() {
        refusedCount += 1
        notifyListeners()
    }

    private fun prunePending() {
        pending.removeAll { it.owner.get() == null }
    }

    private fun grantNext() {
        if (activeOwner != null) return
        while (pending.isNotEmpty()) {
            val next = pending.removeFirst()
            val owner = next.owner.get() ?: continue
            ownerReference = WeakReference(owner)
            notifyListeners()
            next.onGranted()
            return
        }
    }

    private fun notifyListeners() {
        listeners.toList().forEach { it(this) }
    }

    private class PendingRequest(val owner: WeakReference<Any>, var onGranted: () -> Unit)

    public companion object {
        public val shared: TSDialogPresenter = TSDialogPresenter()
    }
}
