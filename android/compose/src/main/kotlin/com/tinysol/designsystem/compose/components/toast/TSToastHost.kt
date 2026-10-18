package com.tinysol.designsystem.compose.components.toast

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.components.shared.tsReduceMotion
import com.tinysol.designsystem.compose.foundation.rememberTSHaptics
import com.tinysol.designsystem.core.logic.TSToastEntry
import com.tinysol.designsystem.core.logic.TSToastMessage
import com.tinysol.designsystem.core.logic.TSToastPresenter
import com.tinysol.designsystem.core.logic.TSToastQueue
import com.tinysol.designsystem.core.logic.TSToastScheduler
import com.tinysol.designsystem.core.logic.TSToastTimeouts
import com.tinysol.designsystem.core.logic.TSToastTone
import com.tinysol.designsystem.core.style.TSToastStyleSpec
import com.tinysol.designsystem.core.style.resolve

internal object TSToastHostTestTags {
    const val LAYER = "ts.toast.layer"
}

@Suppress("ComposeCompositionLocalUsage")
public val LocalTSToastState: ProvidableCompositionLocal<TSToastState?> = staticCompositionLocalOf { null }

@Stable
public class TSToastState(
    public val queue: TSToastQueue,
    scheduler: TSToastScheduler = TSToastScheduler.Main,
) {
    internal var onShow: (TSToastEntry) -> Unit = {}
    internal val layers = mutableStateListOf<Any>()
    private val presenter = TSToastPresenter(
        queue = queue,
        scheduler = scheduler,
        onShow = { onShow(it) },
        onChange = { current = it },
    )

    public var current: TSToastEntry? by mutableStateOf(queue.current)
        private set

    public fun show(
        text: String,
        tone: TSToastTone = TSToastTone.Neutral,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
    ): TSToastEntry = queue.enqueue(TSToastMessage(text, tone, actionLabel, onAction))

    public fun show(message: TSToastMessage): TSToastEntry = queue.enqueue(message)

    public fun dismiss(entry: TSToastEntry): Boolean = presenter.dismiss(entry)

    internal fun performAction(entry: TSToastEntry): Boolean = presenter.performAction(entry)

    internal fun start() = presenter.start()

    internal fun stop() = presenter.stop()
}

@Composable
public fun rememberTSToastState(
    queue: TSToastQueue? = null,
    scheduler: TSToastScheduler = TSToastScheduler.Main,
): TSToastState {
    return remember(queue, scheduler) {
        TSToastState(queue ?: TSToastQueue(TSToastTimeouts.default), scheduler)
    }
}

@Composable
public fun TSToastHost(
    state: TSToastState,
    modifier: Modifier = Modifier,
    bottomInset: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    val haptics = rememberTSHaptics()
    DisposableEffect(state, haptics) {
        state.onShow = { entry -> TSToastStyleSpec.hapticEvent(entry.message.tone)?.let(haptics::perform) }
        state.start()
        onDispose {
            state.stop()
            state.onShow = {}
        }
    }
    CompositionLocalProvider(LocalTSToastState provides state) {
        Box(modifier) {
            content()
            TSToastLayer(state, Modifier.matchParentSize(), bottomInset)
        }
    }
}

@Composable
internal fun TSToastLayer(
    state: TSToastState,
    modifier: Modifier = Modifier,
    bottomInset: Dp = 0.dp,
) {
    val token = remember { Any() }
    DisposableEffect(state, token) {
        state.layers.add(token)
        onDispose { state.layers.remove(token) }
    }
    val entry = if (state.layers.lastOrNull() === token) state.current else null
    val resolved = TSToastStyleSpec.resolve(TSToastStyleSpec.Configuration())
    val reduceMotion = tsReduceMotion()
    Box(
        modifier = modifier
            .testTag(TSToastHostTestTags.LAYER)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(bottom = bottomInset)
            .padding(resolved.outerMarginDp.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedContent(
            targetState = entry,
            transitionSpec = { toastTransition(resolved, reduceMotion) },
            contentAlignment = Alignment.BottomCenter,
            contentKey = { it?.id },
            label = "ts.toast",
        ) { shown ->
            if (shown != null) {
                Box(Modifier.pointerInput(shown.id) { detectTapGestures { } }) {
                    TSToast(
                        message = shown.message.text,
                        tone = shown.message.tone,
                        actionLabel = shown.message.actionLabel,
                        onAction = shown.message.actionLabel?.let { { state.performAction(shown) } },
                    )
                }
            }
        }
    }
}

private fun toastTransition(resolved: TSToastStyleSpec.Resolved, reduceMotion: Boolean): ContentTransform {
    val enter = fadeIn(tween(resolved.enterDurationMillis))
    val exit = fadeOut(tween(resolved.exitDurationMillis))
    return if (reduceMotion) {
        enter togetherWith exit
    } else {
        (enter + slideInVertically(tween(resolved.enterDurationMillis)) { it / 2 }) togetherWith exit
    }
}
