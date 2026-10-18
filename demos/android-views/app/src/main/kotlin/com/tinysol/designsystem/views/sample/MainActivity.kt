package com.tinysol.designsystem.views.sample

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.tinysol.designsystem.sample.support.settings.DemoLaunchOptions
import com.tinysol.designsystem.sample.support.settings.DemoLaunchOverrides
import com.tinysol.designsystem.sample.support.settings.DemoSettings
import com.tinysol.designsystem.sample.support.settings.DemoSettingsListener
import com.tinysol.designsystem.sample.support.settings.DemoSettingsStore
import com.tinysol.designsystem.views.sample.shell.DemoSession
import com.tinysol.designsystem.views.sample.shell.changesConfigurationOf
import com.tinysol.designsystem.views.sample.shell.isNightConfiguration
import com.tinysol.designsystem.views.sample.shell.nightMode
import com.tinysol.designsystem.views.sample.shell.openPage
import com.tinysol.designsystem.views.sample.shell.withDemoSettings
import com.tinysol.designsystem.views.components.toast.TSToastHost

class MainActivity : AppCompatActivity() {
    private lateinit var appliedSettings: DemoSettings
    private var toastHost: TSToastHost? = null
    private val settingsListener = DemoSettingsListener(::onSettingsChanged)

    private val store: DemoSettingsStore
        get() = DemoSettingsStore.from(this)

    override fun attachBaseContext(newBase: Context) {
        appliedSettings = DemoSettingsStore.from(newBase).settings
        AppCompatDelegate.setDefaultNightMode(appliedSettings.theme.nightMode)
        super.attachBaseContext(newBase.withDemoSettings(appliedSettings))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val launchOptions = DemoLaunchOptions.from { key -> intent.getStringExtra(key) }
        if (savedInstanceState == null) {
            if (launchOptions.hasSettings) store.update(launchOptions::applyTo)
            applyThemeOverride(intent)
        }
        AppCompatDelegate.setDefaultNightMode(store.settings.theme.nightMode)
        super.onCreate(savedInstanceState)
        applySystemBarStyle()
        setContentView(R.layout.activity_main)
        window.decorView.layoutDirection = resources.configuration.layoutDirection
        toastHost = TSToastHost.install(this, ViewModelProvider(this)[DemoSession::class.java].toasts)
        if (savedInstanceState == null) {
            launchOptions.page?.let { page ->
                val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
                navHost.navController.openPage(this, page)
            }
        }
        store.addListener(settingsListener)
        val current = store.settings
        if (current.changesConfigurationOf(appliedSettings)) {
            recreate()
        } else {
            appliedSettings = current
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        applyThemeOverride(intent)
    }

    override fun onDestroy() {
        store.removeListener(settingsListener)
        toastHost?.detach()
        toastHost = null
        super.onDestroy()
    }

    private fun applyThemeOverride(intent: Intent) {
        val theme = DemoLaunchOverrides.theme(
            extra = intent.getStringExtra(DemoLaunchOverrides.THEME_EXTRA),
            link = intent.dataString,
            scheme = DemoLaunchOverrides.VIEWS_SCHEME,
        ) ?: return
        store.update { it.copy(theme = theme) }
    }

    private fun applySystemBarStyle() {
        val style = if (isNightConfiguration()) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }

    private fun onSettingsChanged(settings: DemoSettings) {
        val previous = appliedSettings
        appliedSettings = settings
        when {
            settings.changesConfigurationOf(previous) -> recreate()
            settings.theme != previous.theme -> AppCompatDelegate.setDefaultNightMode(settings.theme.nightMode)
        }
    }
}
