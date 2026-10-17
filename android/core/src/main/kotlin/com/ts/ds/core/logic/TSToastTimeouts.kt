package com.ts.ds.core.logic

import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityManager

public object TSToastTimeouts {
    public const val DEFAULT_MILLIS: Long = 4_000L
    public const val SCREEN_READER_MILLIS: Long = 10_000L
    public const val RECOMMENDED_TIMEOUT_MIN_SDK: Int = Build.VERSION_CODES.Q

    public fun resolve(
        hasAction: Boolean,
        screenReaderEnabled: Boolean,
        sdkInt: Int,
        recommendedTimeout: (originalMillis: Long) -> Long,
    ): Long? = when {
        hasAction && screenReaderEnabled -> null
        sdkInt >= RECOMMENDED_TIMEOUT_MIN_SDK -> recommendedTimeout(DEFAULT_MILLIS)
        screenReaderEnabled -> SCREEN_READER_MILLIS
        else -> DEFAULT_MILLIS
    }

    public fun resolve(context: Context, hasAction: Boolean): Long? {
        val manager = context.getSystemService(AccessibilityManager::class.java)
        val screenReaderEnabled = manager?.isTouchExplorationEnabled == true
        return resolve(hasAction, screenReaderEnabled, Build.VERSION.SDK_INT) { original ->
            if (manager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val textAndIcons = AccessibilityManager.FLAG_CONTENT_TEXT or AccessibilityManager.FLAG_CONTENT_ICONS
                val flags = if (hasAction) textAndIcons or AccessibilityManager.FLAG_CONTENT_CONTROLS else textAndIcons
                manager.getRecommendedTimeoutMillis(original.toInt(), flags).toLong()
            } else {
                original
            }
        }
    }

    public fun timeoutFor(context: Context): (TSToastMessage) -> Long? {
        val appContext = context.applicationContext
        return { message -> resolve(appContext, message.hasAction) }
    }
}
