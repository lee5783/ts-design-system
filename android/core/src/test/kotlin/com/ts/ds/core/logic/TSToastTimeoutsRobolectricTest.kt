package com.ts.ds.core.logic

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class TSToastTimeoutsRobolectricTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private fun setScreenReader(enabled: Boolean) {
        shadowOf(context.getSystemService(AccessibilityManager::class.java)).setTouchExplorationEnabled(enabled)
    }

    @Test
    @Config(sdk = [26, 28])
    fun fallbackBelowApi29() {
        setScreenReader(false)
        assertEquals(4_000L, TSToastTimeouts.resolve(context, hasAction = false))
        assertEquals(4_000L, TSToastTimeouts.resolve(context, hasAction = true))

        setScreenReader(true)
        assertEquals(10_000L, TSToastTimeouts.resolve(context, hasAction = false))
        assertNull(TSToastTimeouts.resolve(context, hasAction = true))
    }

    @Test
    @Config(sdk = [29, 36])
    fun recommendedTimeoutFromApi29() {
        setScreenReader(false)
        shadowOf(context.getSystemService(AccessibilityManager::class.java)).apply {
            setNonInteractiveUiTimeout(12_000)
            setInteractiveUiTimeout(20_000)
        }

        assertEquals(12_000L, TSToastTimeouts.resolve(context, hasAction = false))
        assertEquals(20_000L, TSToastTimeouts.resolve(context, hasAction = true))

        setScreenReader(true)
        assertNull(TSToastTimeouts.resolve(context, hasAction = true))
    }

    @Test
    @Config(sdk = [26, 36])
    fun queueBuiltFromContextUsesTheResolver() {
        setScreenReader(false)
        val queue = TSToastQueue(TSToastTimeouts.timeoutFor(context))

        assertEquals(4_000L, queue.enqueue(TSToastMessage("Saved")).timeoutMillis)
    }
}
