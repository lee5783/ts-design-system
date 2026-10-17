package com.ts.ds.views.components.toast

import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.ts.ds.core.logic.TSToastTone
import com.ts.ds.views.R
import com.ts.ds.views.components.hostInActivity
import com.ts.ds.views.components.tapCenter
import com.ts.ds.views.foundation.TSTouchDelegateGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "w600dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSToastViewTest {
    private var actions = 0

    private fun toast(configure: TSToastView.() -> Unit = {}): TSToastView = hostInActivity { context ->
        TSToastView(context).apply {
            message = "Saved"
            configure()
        }
    }

    private fun View.node(): AccessibilityNodeInfoCompat =
        AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(this, it) }

    @Test
    fun messageIsOneElementPerTone() {
        val labels = mapOf(
            TSToastTone.Neutral to "Saved",
            TSToastTone.Success to "Success, Saved",
            TSToastTone.Warning to "Warning, Saved",
            TSToastTone.Danger to "Error, Saved",
            TSToastTone.Info to "Information, Saved",
        )
        labels.forEach { (tone, label) ->
            val view = toast { this.tone = tone }

            assertEquals(label, view.messageView.node().contentDescription)
            assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO, view.textView.importantForAccessibility)
            assertEquals(if (tone == TSToastTone.Neutral) View.GONE else View.VISIBLE, view.iconView.visibility)
        }
    }

    @Test
    fun actionIsAnUnderlinedButtonThatFires() {
        val view = toast {
            actionLabel = "Undo"
            onAction = { actions++ }
        }

        assertEquals(View.VISIBLE, view.actionView.visibility)
        assertEquals("android.widget.Button", view.actionView.node().className)
        assertTrue(view.actionView.paintFlags and Paint.UNDERLINE_TEXT_FLAG != 0)
        view.actionView.tapCenter()

        assertEquals(1, actions)
    }

    @Test
    fun noActionWithoutAHandler() {
        val view = toast { actionLabel = "Undo" }

        assertEquals(View.GONE, view.actionView.visibility)
    }

    @Test
    fun geometryMatchesTheSpec() {
        val plain = toast()
        val withAction = toast {
            actionLabel = "Undo"
            onAction = {}
        }
        val long = toast { message = "A".repeat(300) }

        assertEquals(44, plain.height)
        assertEquals(44, withAction.height)
        assertEquals(420, long.width)
        val bounds: Rect = (withAction.touchDelegate as TSTouchDelegateGroup).bounds.getValue(withAction.actionView)
        assertTrue(bounds.height() >= 48)
        assertTrue(bounds.width() >= 48)
    }

    @Test
    fun xmlAttributesConfigureTheToast() {
        val view = hostInActivity { context ->
            val attrs = Robolectric.buildAttributeSet()
                .addAttribute(R.attr.tsToastTone, "danger")
                .addAttribute(R.attr.tsMessage, "Upload failed")
                .addAttribute(R.attr.tsActionLabel, "Retry")
                .build()
            FrameLayout(context).apply { addView(TSToastView(context, attrs)) }
        }.getChildAt(0) as TSToastView

        assertEquals(TSToastTone.Danger, view.tone)
        assertEquals("Error, Upload failed", view.accessibilityLabel)
        assertEquals("Retry", view.actionLabel.toString())
    }
}
