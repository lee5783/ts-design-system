package com.tinysol.designsystem.views.sample.shell

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.settings.DemoSettings
import com.tinysol.designsystem.sample.support.settings.DemoTheme
import com.tinysol.designsystem.views.sample.R
import java.util.Locale

private val rightToLeftLocale: Locale = Locale.forLanguageTag("ar")

val DemoTheme.nightMode: Int
    get() = when (this) {
        DemoTheme.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        DemoTheme.Light -> AppCompatDelegate.MODE_NIGHT_NO
        DemoTheme.Dark -> AppCompatDelegate.MODE_NIGHT_YES
    }

val DemoTheme.labelRes: Int
    @StringRes get() = when (this) {
        DemoTheme.System -> DemoR.string.demo_settings_theme_system
        DemoTheme.Light -> DemoR.string.demo_settings_theme_light
        DemoTheme.Dark -> DemoR.string.demo_settings_theme_dark
    }

val DemoTheme.markRes: Int
    @DrawableRes get() = when (this) {
        DemoTheme.System -> R.drawable.demo_ic_theme_system
        DemoTheme.Light -> R.drawable.demo_ic_theme_light
        DemoTheme.Dark -> R.drawable.demo_ic_theme_dark
    }

fun Context.isNightConfiguration(): Boolean =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Context.withDemoSettings(settings: DemoSettings): Context {
    val configuration = Configuration(resources.configuration)
    configuration.fontScale *= settings.fontScale
    if (settings.rtl) {
        configuration.setLayoutDirection(rightToLeftLocale)
    }
    return createConfigurationContext(configuration)
}

fun DemoSettings.changesConfigurationOf(applied: DemoSettings): Boolean =
    fontScale != applied.fontScale || rtl != applied.rtl
