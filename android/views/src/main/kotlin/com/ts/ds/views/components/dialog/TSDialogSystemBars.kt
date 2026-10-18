package com.ts.ds.views.components.dialog

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

private val SYSTEM_BAR_TYPES = listOf(
    WindowInsetsCompat.Type.statusBars() to View.SYSTEM_UI_FLAG_FULLSCREEN,
    WindowInsetsCompat.Type.navigationBars() to View.SYSTEM_UI_FLAG_HIDE_NAVIGATION,
)

internal class TSSystemBarsState(val hiddenTypes: Int, val behavior: Int)

internal fun Window.systemBarsState(): TSSystemBarsState {
    val hiddenTypes = SYSTEM_BAR_TYPES.fold(0) { types, (type, legacyFlag) ->
        if (showsSystemBar(type, legacyFlag)) types else types or type
    }
    return TSSystemBarsState(hiddenTypes, WindowCompat.getInsetsController(this, decorView).systemBarsBehavior)
}

internal fun Window.applySystemBars(state: TSSystemBarsState) {
    if (state.hiddenTypes == 0) return
    WindowCompat.getInsetsController(this, decorView).apply {
        systemBarsBehavior = state.behavior
        hide(state.hiddenTypes)
    }
}

@Suppress("DEPRECATION")
private fun Window.showsSystemBar(type: Int, legacyFlag: Int): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        ViewCompat.getRootWindowInsets(decorView)?.isVisible(type) ?: true
    } else {
        val fullscreen = type == WindowInsetsCompat.Type.statusBars() &&
            attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN != 0
        !fullscreen && decorView.systemUiVisibility and legacyFlag == 0
    }
