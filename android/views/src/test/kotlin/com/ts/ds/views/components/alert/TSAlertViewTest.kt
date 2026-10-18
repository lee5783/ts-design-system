package com.ts.ds.views.components.alert

import android.graphics.Rect
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.ts.ds.core.style.TSAlertTone
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.views.components.button.TSButtonView
import com.ts.ds.views.components.hostInActivity
import com.ts.ds.views.components.tapCenter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "w360dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSAlertViewTest {
    private var dismissals = 0
    private var retries = 0

    private fun alert(configure: TSAlertView.() -> Unit = {}): TSAlertView = hostInActivity { context ->
        LinearLayout(context).apply {
            addView(
                TSAlertView(context).apply {
                    message = "Free up space"
                    configure()
                },
                LinearLayout.LayoutParams(320, LinearLayout.LayoutParams.WRAP_CONTENT),
            )
        }
    }.getChildAt(0) as TSAlertView

    private fun View.boundsIn(ancestor: View): Rect {
        val location = IntArray(2).also(::getLocationInWindow)
        val origin = IntArray(2).also(ancestor::getLocationInWindow)
        return Rect(location[0] - origin[0], location[1] - origin[1], location[0] - origin[0] + width, location[1] - origin[1] + height)
    }

    private fun View.tapAt(x: Float, y: Float) {
        val time = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
            val event = MotionEvent.obtain(time, time, action, x, y, 0)
            dispatchTouchEvent(event)
            event.recycle()
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun dismissFiresFromTouch() {
        val view = alert { onDismiss = { dismissals++ } }

        view.dismissButton.tapCenter()

        assertEquals(1, dismissals)
    }

    @Test
    fun noDismissWithoutACallback() {
        val view = alert()

        assertEquals(View.GONE, view.dismissButton.visibility)
    }

    @Test
    fun dismissHitAreaReachesBeyondTheGlyph() {
        val view = alert { onDismiss = { dismissals++ } }
        val glyph = view.dismissButton.boundsIn(view)

        view.tapAt(glyph.right + 10f, glyph.exactCenterY())

        assertEquals(1, dismissals)
        assertEquals(20, glyph.width())
    }

    @Test
    fun actionStaysSeparateBeforeTheDismissButton() {
        lateinit var retry: TSButtonView
        val view = alert {
            tone = TSAlertTone.Danger
            message = "Upload failed"
            onDismiss = { dismissals++ }
            retry = TSButtonView(context).apply {
                text = "Retry"
                variant = TSButtonVariant.Ghost
                setOnClickListener { retries++ }
            }
            actionView = retry
        }

        assertTrue(retry.boundsIn(view).right <= view.dismissButton.boundsIn(view).left)
        retry.tapCenter()
        assertEquals(1, retries)
        assertEquals(0, dismissals)
    }

    @Test
    fun heightsMatchTheDesignAtTheDefaultSize() {
        assertEquals(46, alert { message = "Saved"; onDismiss = {} }.height)
        assertEquals(66, alert { message = "Saved"; title = "Done" }.height)
        assertFalse(alert().messageView.isClickable)
    }
}
