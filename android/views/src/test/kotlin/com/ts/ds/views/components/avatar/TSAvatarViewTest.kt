package com.ts.ds.views.components.avatar

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.widget.TextViewCompat
import com.ts.ds.core.style.TSAvatarContent
import com.ts.ds.core.style.TSAvatarSize
import com.ts.ds.views.R
import com.ts.ds.views.components.hostInActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSAvatarViewTest {
    private fun avatar(configure: TSAvatarView.() -> Unit = {}): TSAvatarView = hostInActivity { context ->
        TSAvatarView(context).apply {
            name = "Ada Lovelace"
            configure()
        }
    }

    private fun TSAvatarView.photo(): BitmapDrawable =
        BitmapDrawable(resources, Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888))

    @Test
    fun labelIsTheNameWithTheImageClassAndNoClick() {
        val view = avatar()
        val info = AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(view, it) }

        assertEquals("Ada Lovelace", info.contentDescription.toString())
        assertEquals(ImageView::class.java.name, info.className)
        assertFalse(info.isClickable)
        assertFalse(view.isClickable)
        assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_YES, view.importantForAccessibility)
    }

    @Test
    fun initialsWhenThereIsNoImage() {
        val view = avatar()

        assertEquals(TSAvatarContent.Initials, view.content)
        assertEquals("AL", view.initialsLabel.text.toString())
        assertEquals(View.VISIBLE, view.initialsLabel.visibility)
        assertEquals(1, view.initialsLabel.maxLines)
        assertEquals(TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM, TextViewCompat.getAutoSizeTextType(view.initialsLabel))
        assertEquals(14, TextViewCompat.getAutoSizeMaxTextSize(view.initialsLabel))
        assertEquals(7, TextViewCompat.getAutoSizeMinTextSize(view.initialsLabel))
    }

    @Test
    fun imageReplacesTheInitialsAndClearingItRestoresThem() {
        val view = avatar()
        view.image = view.photo()
        assertEquals(TSAvatarContent.Image, view.content)
        assertEquals(View.GONE, view.initialsLabel.visibility)

        view.image = null
        assertEquals(TSAvatarContent.Initials, view.content)
        assertEquals(View.VISIBLE, view.initialsLabel.visibility)
    }

    @Test
    fun failedImageFallsBackToInitials() {
        val view = avatar()
        view.image = BitmapDrawable(view.resources, BitmapFactory.decodeByteArray(byteArrayOf(1, 2, 3), 0, 3))

        assertEquals(TSAvatarContent.Initials, view.content)
        assertEquals("AL", view.initialsLabel.text.toString())
    }

    @Test
    fun blankNameShowsThePlaceholder() {
        val view = avatar { name = " " }
        val info = AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(view, it) }

        assertEquals(TSAvatarContent.Placeholder, view.content)
        assertEquals(View.GONE, view.initialsLabel.visibility)
        assertNull(info.contentDescription)
    }

    @Test
    fun circleIsFixedPerSize() {
        listOf(TSAvatarSize.Xs to 24, TSAvatarSize.Sm to 32, TSAvatarSize.Md to 40, TSAvatarSize.Lg to 56).forEach { (size, px) ->
            val view = avatar { this.size = size }
            assertEquals(px, view.width)
            assertEquals(px, view.height)
        }
    }

    @Test
    @Config(fontScale = 2f)
    fun circleAndInitialsDoNotScaleAt200PercentText() {
        val view = avatar { size = TSAvatarSize.Lg }

        assertEquals(56, view.width)
        assertEquals(20, TextViewCompat.getAutoSizeMaxTextSize(view.initialsLabel))
        assertEquals(20f, view.initialsLabel.textSize, 0.5f)
    }

    @Test
    fun xmlAttributesConfigureTheAvatar() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(R.attr.tsName, "Grace Hopper")
            .addAttribute(R.attr.tsAvatarSize, "lg")
            .build()
        val view = hostInActivity { TSAvatarView(it, attrs) }

        assertEquals("Grace Hopper", view.name)
        assertEquals(TSAvatarSize.Lg, view.size)
        assertEquals("GH", view.initialsLabel.text.toString())
    }
}
