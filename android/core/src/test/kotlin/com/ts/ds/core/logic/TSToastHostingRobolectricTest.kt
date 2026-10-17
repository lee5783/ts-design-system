package com.ts.ds.core.logic

import android.content.Context
import android.os.Looper
import android.view.accessibility.AccessibilityManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class TSToastHostingRobolectricTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private fun setScreenReader(enabled: Boolean) {
        shadowOf(context.getSystemService(AccessibilityManager::class.java)).setTouchExplorationEnabled(enabled)
    }

    private fun presenter(): TSToastPresenter =
        TSToastPresenter(TSToastQueue(TSToastTimeouts.timeoutFor(context)), TSToastScheduler.Main).apply { start() }

    private fun advance(millis: Long) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis))
    }

    @Test
    @Config(sdk = [26, 28])
    fun mainSchedulerUsesTheFallbackBelowApi29() {
        setScreenReader(true)
        val presenter = presenter()
        presenter.queue.enqueue(TSToastMessage("Saved"))

        advance(9_999)
        assertEquals("Saved", presenter.current?.message?.text)
        advance(1)
        assertNull(presenter.current)

        presenter.queue.enqueue(TSToastMessage("Message deleted", actionLabel = "Undo", onAction = {}))
        advance(60_000)
        assertEquals("Message deleted", presenter.current?.message?.text)
    }

    @Test
    @Config(sdk = [29, 36])
    fun mainSchedulerUsesTheRecommendedTimeoutFromApi29() {
        setScreenReader(false)
        shadowOf(context.getSystemService(AccessibilityManager::class.java)).apply {
            setNonInteractiveUiTimeout(12_000)
            setInteractiveUiTimeout(20_000)
        }
        val presenter = presenter()
        presenter.queue.enqueue(TSToastMessage("Saved"))

        advance(11_999)
        assertEquals("Saved", presenter.current?.message?.text)
        advance(1)
        assertNull(presenter.current)

        presenter.queue.enqueue(TSToastMessage("Message deleted", actionLabel = "Undo", onAction = {}))
        advance(19_999)
        assertEquals("Message deleted", presenter.current?.message?.text)
        advance(1)
        assertNull(presenter.current)
    }

    @Test
    @Config(sdk = [26, 36])
    fun accessibilityLabelAndAnnouncement() {
        assertEquals("Saved", TSToastAccessibility.label(context, TSToastTone.Neutral, "Saved"))
        assertEquals("Success, Saved", TSToastAccessibility.label(context, TSToastTone.Success, "Saved"))
        assertEquals("Information, Synced", TSToastAccessibility.label(context, TSToastTone.Info, "Synced"))
        assertEquals("Warning, Low storage", TSToastAccessibility.label(context, TSToastTone.Warning, "Low storage"))
        assertEquals(
            "Error, Message deleted, Undo",
            TSToastAccessibility.announcement(context, TSToastMessage("Message deleted", TSToastTone.Danger, "Undo")),
        )
        assertNull(TSToastAccessibility.toneName(context, TSToastTone.Neutral))
    }
}
