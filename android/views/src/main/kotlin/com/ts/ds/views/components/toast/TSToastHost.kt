package com.ts.ds.views.components.toast

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewGroup
import com.ts.ds.core.haptics.DsHaptics
import com.ts.ds.core.logic.TSToastEntry
import com.ts.ds.core.logic.TSToastMessage
import com.ts.ds.core.logic.TSToastPresenter
import com.ts.ds.core.logic.TSToastQueue
import com.ts.ds.core.logic.TSToastScheduler
import com.ts.ds.core.logic.TSToastTimeouts
import com.ts.ds.core.logic.TSToastTone
import com.ts.ds.core.style.TSToastStyleSpec
import com.ts.ds.views.R

public class TSToastHost @JvmOverloads constructor(
    public val queue: TSToastQueue,
    scheduler: TSToastScheduler = TSToastScheduler.Main,
) {
    private val presenter = TSToastPresenter(queue, scheduler, ::playShowEffects, ::render)
    private val layers = mutableListOf<TSToastLayer>()
    private var rootLayer: TSToastLayer? = null
    private var announcedId: Long? = null

    public var anchorView: View? = null
        set(value) {
            field = value
            rootLayer?.anchorView = value
        }

    public var bottomInset: Int = 0
        set(value) {
            field = value
            rootLayer?.bottomInset = value
        }

    public val current: TSToastEntry?
        get() = queue.current

    public val isAttached: Boolean
        get() = rootLayer != null

    internal val topLayer: TSToastLayer?
        get() = layers.lastOrNull()

    internal var reduceMotion: (() -> Boolean)? = null
        set(value) {
            field = value
            value?.let { reduce -> layers.forEach { it.reduceMotion = reduce } }
        }

    public fun show(
        text: CharSequence,
        tone: TSToastTone = TSToastTone.Neutral,
        actionLabel: CharSequence? = null,
        onAction: (() -> Unit)? = null,
    ): TSToastEntry = queue.enqueue(TSToastMessage(text.toString(), tone, actionLabel?.toString(), onAction))

    public fun show(message: TSToastMessage): TSToastEntry = queue.enqueue(message)

    public fun dismiss(entry: TSToastEntry): Boolean = presenter.dismiss(entry)

    public fun attach(container: ViewGroup) {
        detach()
        val layer = makeLayer(container).also {
            it.anchorView = anchorView
            it.bottomInset = bottomInset
        }
        rootLayer = layer
        layers.add(0, layer)
        container.setTag(R.id.ts_toast_host, this)
        presenter.start()
        render(queue.current)
    }

    public fun detach() {
        val layer = rootLayer ?: return
        presenter.stop()
        (layer.parent as? ViewGroup)?.let {
            it.setTag(R.id.ts_toast_host, null)
            it.removeView(layer)
        }
        layer.anchorView = null
        layers.remove(layer)
        rootLayer = null
    }

    public fun attachOverlay(container: ViewGroup) {
        if (layers.any { it.parent === container }) return
        topLayer?.clear()
        layers.add(makeLayer(container))
        render(queue.current)
    }

    public fun detachOverlay(container: ViewGroup) {
        val layer = layers.firstOrNull { it.parent === container && it !== rootLayer } ?: return
        layer.clear()
        container.removeView(layer)
        layers.remove(layer)
        render(queue.current)
    }

    private fun makeLayer(container: ViewGroup): TSToastLayer = TSToastLayer.attach(container).also { layer ->
        reduceMotion?.let { layer.reduceMotion = it }
    }

    private fun render(entry: TSToastEntry?) {
        if (!presenter.isStarted) return
        val announces = entry != null && entry.id != announcedId
        if (entry != null) announcedId = entry.id
        topLayer?.show(entry, announces) { presenter.performAction(it) }
    }

    private fun playShowEffects(entry: TSToastEntry) {
        val event = TSToastStyleSpec.hapticEvent(entry.message.tone) ?: return
        topLayer?.performHapticFeedback(DsHaptics.constantFor(event))
    }

    public companion object {
        @JvmStatic
        @JvmOverloads
        public fun install(
            activity: Activity,
            queue: TSToastQueue = TSToastQueue(TSToastTimeouts.timeoutFor(activity)),
            scheduler: TSToastScheduler = TSToastScheduler.Main,
        ): TSToastHost {
            val content = activity.findViewById<ViewGroup>(android.R.id.content)
            (content.getTag(R.id.ts_toast_host) as? TSToastHost)?.let { return it }
            return TSToastHost(queue, scheduler).also { it.attach(content) }
        }

        @JvmStatic
        public fun from(context: Context): TSToastHost? {
            var current: Context? = context
            while (current != null) {
                if (current is Activity) {
                    return current.findViewById<ViewGroup>(android.R.id.content)?.getTag(R.id.ts_toast_host) as? TSToastHost
                }
                current = (current as? ContextWrapper)?.baseContext
            }
            return null
        }
    }
}
