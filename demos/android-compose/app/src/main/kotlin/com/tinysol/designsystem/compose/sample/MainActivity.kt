package com.tinysol.designsystem.compose.sample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tinysol.designsystem.compose.sample.shell.DemoApp
import com.tinysol.designsystem.sample.support.settings.DemoLaunchOptions
import com.tinysol.designsystem.sample.support.settings.DemoLaunchOverrides
import com.tinysol.designsystem.sample.support.settings.DemoSettingsStore

class MainActivity : ComponentActivity() {
    private val store: DemoSettingsStore
        get() = DemoSettingsStore.from(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val launchOptions = DemoLaunchOptions.from { key -> intent.getStringExtra(key) }
        if (savedInstanceState == null) {
            if (launchOptions.hasSettings) store.update(launchOptions::applyTo)
            applyThemeOverride(intent)
        }
        setContent {
            DemoApp(
                store = store,
                initialPage = launchOptions.page,
                onDarkThemeChanged = ::applySystemBarStyle,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        applyThemeOverride(intent)
    }

    private fun applyThemeOverride(intent: Intent) {
        val theme = DemoLaunchOverrides.theme(
            extra = intent.getStringExtra(DemoLaunchOverrides.THEME_EXTRA),
            link = intent.dataString,
            scheme = DemoLaunchOverrides.COMPOSE_SCHEME,
        ) ?: return
        store.update { it.copy(theme = theme) }
    }

    private fun applySystemBarStyle(darkTheme: Boolean) {
        val style = if (darkTheme) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}
