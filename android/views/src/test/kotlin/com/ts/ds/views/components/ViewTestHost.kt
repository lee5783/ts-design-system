package com.ts.ds.views.components

import android.app.Activity
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import com.ts.ds.views.R
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

internal fun <T : View> hostInActivity(create: (ContextThemeWrapper) -> T): T {
    val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
    val context = ContextThemeWrapper(activity, R.style.Theme_TS)
    val view = create(context)
    val root = FrameLayout(context)
    root.addView(view, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    activity.setContentView(root)
    shadowOf(Looper.getMainLooper()).idle()
    return view
}

internal fun View.tapCenter() {
    val time = SystemClock.uptimeMillis()
    listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
        val event = MotionEvent.obtain(time, time, action, width / 2f, height / 2f, 0)
        dispatchTouchEvent(event)
        event.recycle()
    }
    shadowOf(Looper.getMainLooper()).idle()
}
