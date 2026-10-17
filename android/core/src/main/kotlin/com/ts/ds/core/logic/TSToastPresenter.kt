package com.ts.ds.core.logic

import java.lang.ref.WeakReference

public class TSToastPresenter(
    public val queue: TSToastQueue,
    private val scheduler: TSToastScheduler = TSToastScheduler.Main,
    private val onShow: (TSToastEntry) -> Unit = {},
    private val onChange: (TSToastEntry?) -> Unit = {},
) {
    private val listener: (TSToastEntry?) -> Unit = ::present
    private var timer: TSToastScheduler.Cancellable? = null
    private var shownId: Long? = null

    public var isStarted: Boolean = false
        private set

    public val current: TSToastEntry?
        get() = queue.current

    public fun start() {
        if (isStarted) return
        isStarted = true
        queue.addListener(listener)
        present(queue.current)
    }

    public fun stop() {
        if (!isStarted) return
        isStarted = false
        queue.removeListener(listener)
        timer?.cancel()
        timer = null
    }

    public fun performAction(entry: TSToastEntry): Boolean = queue.performAction(entry.id)

    public fun dismiss(entry: TSToastEntry): Boolean = queue.dismiss(entry.id)

    private fun present(entry: TSToastEntry?) {
        timer?.cancel()
        timer = null
        if (entry != null && entry.id != shownId) {
            shownId = entry.id
            onShow(entry)
        }
        if (entry == null) shownId = null
        entry?.timeoutMillis?.let { timeout ->
            val queueReference = WeakReference(queue)
            val id = entry.id
            timer = scheduler.schedule(timeout) { queueReference.get()?.dismiss(id) }
        }
        onChange(entry)
    }
}
