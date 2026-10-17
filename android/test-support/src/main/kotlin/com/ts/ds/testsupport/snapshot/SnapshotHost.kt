package com.ts.ds.testsupport.snapshot

import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.activity.ComponentActivity
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController

internal class SnapshotHost private constructor(
    private val controller: ActivityController<ComponentActivity>,
) : AutoCloseable {
    val activity: ComponentActivity
        get() = controller.get()

    fun show(view: View, width: Int) {
        val root = ScrollView(activity).apply { isVerticalScrollBarEnabled = false }
        root.addView(view, FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT))
        activity.setContentView(root)
        shadowOf(Looper.getMainLooper()).idle()
        val location = IntArray(2).also(view::getLocationInWindow)
        val windowHeight = activity.window.decorView.height
        check(location[1] + view.height <= windowHeight) {
            "Snapshot content is ${view.height}px tall and would be clipped by the ${windowHeight}px window; " +
                "raise SnapshotEnvironment.HEIGHT_DP or split the sample"
        }
    }

    override fun close() {
        controller.pause().stop().destroy()
    }

    companion object {
        fun launch(): SnapshotHost = SnapshotHost(Robolectric.buildActivity(ComponentActivity::class.java).setup())
    }
}
