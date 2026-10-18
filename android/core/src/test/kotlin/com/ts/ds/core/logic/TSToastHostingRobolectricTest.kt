package com.ts.ds.core.logic

import android.os.Looper
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
    private fun presenter(): TSToastPresenter =
        TSToastPresenter(TSToastQueue(TSToastTimeouts.default), TSToastScheduler.Main).apply { start() }

    private fun advance(millis: Long) {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis))
    }

    @Test
    @Config(sdk = [26, 36])
    fun mainSchedulerUsesTheDefaultTimeout() {
        val presenter = presenter()
        presenter.queue.enqueue(TSToastMessage("Saved"))

        advance(3_999)
        assertEquals("Saved", presenter.current?.message?.text)
        advance(1)
        assertNull(presenter.current)
    }
}
