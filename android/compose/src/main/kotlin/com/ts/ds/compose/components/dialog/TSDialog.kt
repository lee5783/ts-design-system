package com.ts.ds.compose.components.dialog

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.ts.ds.compose.components.toast.LocalTSToastState
import com.ts.ds.compose.components.toast.TSToastLayer
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.logic.TSDialogPresenter
import com.ts.ds.core.logic.TSDialogRequestResult
import com.ts.ds.core.style.TSDialogStyleSpec
import com.ts.ds.core.style.TSDialogVariant
import kotlin.coroutines.cancellation.CancellationException

private const val CENTER_FADE_MILLIS = 150

@Composable
public fun TSDialog(
    isPresented: Boolean,
    onPresentedChange: (Boolean) -> Unit,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    variant: TSDialogVariant = TSDialogVariant.Center,
    actions: List<TSDialogAction> = emptyList(),
    onDismiss: (() -> Unit)? = null,
    presenter: TSDialogPresenter = TSDialogPresenter.shared,
    onRefused: (() -> Unit)? = null,
) {
    TSDialog(
        isPresented = isPresented,
        onPresentedChange = onPresentedChange,
        title = title,
        modifier = modifier,
        variant = variant,
        actions = actions,
        onDismiss = onDismiss,
        presenter = presenter,
        onRefused = onRefused,
    ) {
        TSDialogMessage(message, resolveDialogStyle(variant))
    }
}

@Composable
public fun TSDialog(
    isPresented: Boolean,
    onPresentedChange: (Boolean) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    variant: TSDialogVariant = TSDialogVariant.Center,
    actions: List<TSDialogAction> = emptyList(),
    onDismiss: (() -> Unit)? = null,
    presenter: TSDialogPresenter = TSDialogPresenter.shared,
    onRefused: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val owner = remember { Any() }
    var shown by remember(presenter) { mutableStateOf(false) }
    val actionGuard = remember { TSDialogActionGuard() }
    val currentIsPresented by rememberUpdatedState(isPresented)
    val currentOnPresentedChange by rememberUpdatedState(onPresentedChange)
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentOnRefused by rememberUpdatedState(onRefused)

    LaunchedEffect(isPresented, presenter) {
        if (isPresented) actionGuard.dismissing = false
        if (shown) return@LaunchedEffect
        if (!isPresented) {
            presenter.end(owner)
            return@LaunchedEffect
        }
        when (presenter.request(owner) { shown = true }) {
            TSDialogRequestResult.Granted -> shown = true
            TSDialogRequestResult.Queued -> Unit
            TSDialogRequestResult.Refused -> {
                currentOnPresentedChange(false)
                currentOnRefused?.invoke()
            }
        }
    }
    DisposableEffect(presenter) {
        onDispose { presenter.end(owner) }
    }
    if (!shown) return

    val userDismiss: () -> Unit = {
        currentOnPresentedChange(false)
        currentOnDismiss?.invoke()
    }
    val onExited: () -> Unit = {
        shown = false
        presenter.end(owner)
    }
    val onAction = { action: TSDialogAction ->
        if (currentIsPresented && !actionGuard.dismissing) {
            actionGuard.dismissing = action.dismisses
            action.onClick()
            if (action.dismisses) currentOnPresentedChange(false)
        }
    }
    val resolved = resolveDialogStyle(variant)
    val density = LocalDensity.current
    val panel: @Composable (Modifier, Boolean) -> Unit = { panelModifier, scrolls ->
        CompositionLocalProvider(LocalDensity provides density) {
            TSDialogPanel(
                title = title,
                resolved = resolved,
                actions = actions,
                onAction = onAction,
                onClose = userDismiss,
                modifier = modifier.then(panelModifier),
                scrollsContent = scrolls,
                content = content,
            )
        }
    }
    when (variant) {
        TSDialogVariant.Center -> TSCenterDialogHost(isPresented, resolved, userDismiss, onExited, panel)
        TSDialogVariant.Sheet -> TSSheetDialogHost(isPresented, resolved, userDismiss, onExited, panel)
    }
}

@Composable
private fun TSCenterDialogHost(
    visible: Boolean,
    resolved: TSDialogStyleSpec.Resolved,
    onUserDismiss: () -> Unit,
    onExited: () -> Unit,
    panel: @Composable (Modifier, Boolean) -> Unit,
) {
    val visibility = remember { MutableTransitionState(false) }
    LaunchedEffect(visible) { visibility.targetState = visible }
    val alpha by rememberTransition(visibility).animateFloat({ tween(CENTER_FADE_MILLIS) }, label = "ts.dialog.fade") { if (it) 1f else 0f }
    val currentOnExited by rememberUpdatedState(onExited)
    LaunchedEffect(visibility.isIdle, visibility.currentState, visibility.targetState) {
        if (visibility.isIdle && !visibility.currentState && !visibility.targetState && !visible) currentOnExited()
    }
    var backScale by remember { mutableFloatStateOf(1f) }
    val hostWindow = LocalContext.current.hostWindow()
    val hostBars = remember(hostWindow) { hostWindow?.systemBarsState() }

    Dialog(
        onDismissRequest = onUserDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            window?.setDimAmount(0f)
            window?.setWindowAnimations(0)
            if (hostBars != null) window?.applySystemBars(hostBars)
        }
        PredictiveBackHandler(enabled = visible) { progress ->
            try {
                progress.collect { backScale = TSDialogStyleSpec.predictiveBackScale(it.progress) }
                onUserDismiss()
            } catch (cancelled: CancellationException) {
                backScale = 1f
                throw cancelled
            }
        }
        val toastState = LocalTSToastState.current
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha }
                    .background(TSTheme.colors.color(resolved.scrim))
                    .testTag(TSDialogTestTags.SCRIM)
                    .pointerInput(onUserDismiss) { detectTapGestures { onUserDismiss() } }
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(resolved.outerMarginDp.dp),
                contentAlignment = Alignment.Center,
            ) {
                panel(
                    Modifier
                        .graphicsLayer {
                            scaleX = backScale
                            scaleY = backScale
                        }
                        .pointerInput(Unit) { detectTapGestures { } },
                    true,
                )
            }
            if (toastState != null) TSToastLayer(toastState, Modifier.matchParentSize())
        }
    }
    DisposableEffect(hostWindow, hostBars) {
        onDispose { if (hostWindow != null && hostBars != null) hostWindow.applySystemBars(hostBars) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TSSheetDialogHost(
    visible: Boolean,
    resolved: TSDialogStyleSpec.Resolved,
    onUserDismiss: () -> Unit,
    onExited: () -> Unit,
    panel: @Composable (Modifier, Boolean) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentOnExited by rememberUpdatedState(onExited)
    var hideRequested by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (!visible) {
            hideRequested = true
            if (sheetState.isVisible) sheetState.hide()
            currentOnExited()
        } else if (hideRequested) {
            hideRequested = false
            sheetState.show()
        }
    }
    val colors = TSTheme.colors
    val hostWindow = LocalContext.current.hostWindow()
    val hostBars = remember(hostWindow) { hostWindow?.systemBarsState() }
    ModalBottomSheet(
        onDismissRequest = onUserDismiss,
        sheetState = sheetState,
        sheetMaxWidth = Dp.Unspecified,
        shape = resolved.panelShape(),
        containerColor = colors.color(resolved.fill),
        contentColor = colors.textPrimary,
        tonalElevation = 0.dp,
        scrimColor = colors.color(resolved.scrim),
        dragHandle = null,
        contentWindowInsets = { BottomSheetDefaults.windowInsets },
    ) {
        val window = LocalView.current.dialogWindow()
        SideEffect {
            if (hostBars != null) window?.applySystemBars(hostBars)
        }
        val toastState = LocalTSToastState.current
        Box {
            panel(Modifier, true)
            if (toastState != null) TSToastLayer(toastState, Modifier.matchParentSize())
        }
    }
    DisposableEffect(hostWindow, hostBars) {
        onDispose { if (hostWindow != null && hostBars != null) hostWindow.applySystemBars(hostBars) }
    }
}

private class TSDialogActionGuard {
    var dismissing = false
}
