package com.ts.ds.demo.compose.shell

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.ts.ds.compose.components.toast.TSToastHost
import com.ts.ds.demo.compose.components.BottomNavShellScreen
import com.ts.ds.compose.components.toast.rememberTSToastState
import com.ts.ds.compose.theme.TSMotion
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.settings.DemoSettings
import com.ts.ds.demo.support.settings.DemoSettingsListener
import com.ts.ds.demo.support.settings.DemoSettingsStore

@Composable
fun DemoApp(
    store: DemoSettingsStore,
    initialPage: DemoPageId? = null,
    onDarkThemeChanged: (Boolean) -> Unit = {},
) {
    val settings by rememberDemoSettings(store)
    val darkTheme = settings.theme.isDark(isSystemInDarkTheme())
    LaunchedEffect(darkTheme) { onDarkThemeChanged(darkTheme) }

    val density = LocalDensity.current
    val layoutDirection = if (settings.rtl) LayoutDirection.Rtl else LocalLayoutDirection.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * settings.fontScale),
        LocalLayoutDirection provides layoutDirection,
    ) {
        TSTheme(darkTheme = darkTheme) {
            val themeControl = remember(store, settings.theme) {
                DemoThemeControl(settings.theme) { store.update { it.copy(theme = it.theme.next()) } }
            }
            CompositionLocalProvider(LocalDemoThemeControl provides themeControl) {
                TSToastHost(rememberTSToastState(), Modifier.fillMaxSize()) {
                    DemoShell(
                        settings = settings,
                        initialRoute = initialPage?.let(DemoRoute::Page) ?: DemoRoute.Catalog,
                        onSettingsChange = store::update,
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoShell(
    settings: DemoSettings,
    initialRoute: DemoRoute,
    onSettingsChange: ((DemoSettings) -> DemoSettings) -> Unit,
) {
    var routeKey by rememberSaveable { mutableStateOf(initialRoute.key) }
    var settingsVisible by rememberSaveable { mutableStateOf(false) }
    val route = DemoRoute.fromKey(routeKey)
    val navigate: (DemoRoute) -> Unit = { routeKey = it.key }

    val backRoute = if (route == DemoRoute.BottomNavShell) DemoRoute.Page(DemoPageId.BottomNav) else DemoRoute.Catalog
    BackHandler(enabled = route != DemoRoute.Catalog && !settingsVisible) { navigate(backRoute) }
    BackHandler(enabled = settingsVisible) { settingsVisible = false }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TSTheme.colors.backgroundPrimary),
    ) {
        Crossfade(
            targetState = route,
            animationSpec = motionSpec(settings.reduceMotion),
            modifier = if (settingsVisible) Modifier.clearAndSetSemantics {} else Modifier,
            label = "demo-route",
        ) { current ->
            when (current) {
                DemoRoute.Catalog -> CatalogScreen(
                    onOpenPage = { navigate(DemoRoute.Page(it)) },
                    onOpenSettings = { settingsVisible = true },
                    onOpenAbout = { navigate(DemoRoute.About) },
                )
                DemoRoute.About -> AboutScreen(onBack = { navigate(DemoRoute.Catalog) })
                DemoRoute.BottomNavShell -> BottomNavShellScreen(onBack = { navigate(DemoRoute.Page(DemoPageId.BottomNav)) })
                is DemoRoute.Page -> DemoPageScreen(
                    page = current.page,
                    onBack = { navigate(DemoRoute.Catalog) },
                    onOpenSettings = { settingsVisible = true },
                    onOpenBottomNavShell = { navigate(DemoRoute.BottomNavShell) },
                )
            }
        }
        DemoScrim(
            visible = settingsVisible,
            reduceMotion = settings.reduceMotion,
            onDismiss = { settingsVisible = false },
        )
        AnimatedVisibility(
            visible = settingsVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = if (settings.reduceMotion) EnterTransition.None else slideInVertically(motionSpec(false)) { it },
            exit = if (settings.reduceMotion) ExitTransition.None else slideOutVertically(motionSpec(false)) { it },
        ) {
            DemoSettingsSheet(
                settings = settings,
                onSettingsChange = onSettingsChange,
                onClose = { settingsVisible = false },
            )
        }
    }
}

@Composable
private fun DemoScrim(
    visible: Boolean,
    reduceMotion: Boolean,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = if (reduceMotion) EnterTransition.None else fadeIn(motionSpec(false)),
        exit = if (reduceMotion) ExitTransition.None else fadeOut(motionSpec(false)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TSTheme.colors.overlay)
                .dismissOnTap(onDismiss),
        )
    }
}

private fun <T> motionSpec(reduceMotion: Boolean): FiniteAnimationSpec<T> =
    if (reduceMotion) snap() else tween(TSMotion.durationBase, easing = TSMotion.easingStandard)

@Composable
private fun rememberDemoSettings(store: DemoSettingsStore): State<DemoSettings> {
    val state = remember(store) { mutableStateOf(store.settings) }
    DisposableEffect(store) {
        val listener = DemoSettingsListener { state.value = it }
        store.addListener(listener)
        state.value = store.settings
        onDispose { store.removeListener(listener) }
    }
    return state
}
