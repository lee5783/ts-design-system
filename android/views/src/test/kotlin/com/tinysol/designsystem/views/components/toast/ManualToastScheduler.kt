package com.tinysol.designsystem.views.components.toast

import com.tinysol.designsystem.core.logic.TSToastScheduler

class ManualToastScheduler : TSToastScheduler {
    private class Task(val dueAt: Long, val action: () -> Unit) {
        var cancelled = false
    }

    private val tasks = mutableListOf<Task>()

    var now: Long = 0
        private set

    val pendingDelays: List<Long>
        get() = tasks.filterNot { it.cancelled }.map { it.dueAt - now }

    override fun schedule(delayMillis: Long, action: () -> Unit): TSToastScheduler.Cancellable {
        val task = Task(now + delayMillis, action)
        tasks += task
        return TSToastScheduler.Cancellable { task.cancelled = true }
    }

    fun advanceBy(millis: Long) {
        val target = now + millis
        while (true) {
            val next = tasks.filter { !it.cancelled && it.dueAt <= target }.minByOrNull { it.dueAt } ?: break
            tasks.remove(next)
            now = next.dueAt
            next.action()
        }
        now = target
        tasks.removeAll { it.cancelled }
    }
}
