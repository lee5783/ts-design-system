package com.ts.ds.core.logic

public class TSToastQueue(
    private val timeoutFor: (TSToastMessage) -> Long?,
) {
    private val entries = ArrayDeque<TSToastEntry>()
    private val listeners = mutableListOf<(TSToastEntry?) -> Unit>()
    private var nextId = 1L

    public val current: TSToastEntry?
        get() = entries.firstOrNull()

    public val pending: List<TSToastEntry>
        get() = entries.drop(1)

    public val size: Int
        get() = entries.size

    public fun enqueue(message: TSToastMessage): TSToastEntry {
        val entry = TSToastEntry(nextId++, message, timeoutFor(message))
        entries.addLast(entry)
        if (entries.size == 1) notifyListeners()
        return entry
    }

    public fun dismiss(id: Long): Boolean {
        val index = entries.indexOfFirst { it.id == id }
        if (index < 0) return false
        entries.removeAt(index)
        if (index == 0) notifyListeners()
        return true
    }

    public fun dismissCurrent(): Boolean = current?.let { dismiss(it.id) } ?: false

    public fun performAction(id: Long): Boolean {
        val entry = entries.firstOrNull { it.id == id } ?: return false
        entry.message.onAction?.invoke()
        return dismiss(id)
    }

    public fun clear() {
        if (entries.isEmpty()) return
        entries.clear()
        notifyListeners()
    }

    public fun addListener(listener: (TSToastEntry?) -> Unit) {
        listeners += listener
    }

    public fun removeListener(listener: (TSToastEntry?) -> Unit) {
        listeners -= listener
    }

    private fun notifyListeners() {
        val head = current
        listeners.toList().forEach { it(head) }
    }
}
