package com.ts.ds.core.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSAvatarImageTest {
    private val resources
        get() = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun nilIsNotUsable() {
        assertNull(TSAvatarImage.usable(null))
    }

    @Test
    fun aDecodedBitmapIsUsable() {
        val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        assertNotNull(TSAvatarImage.usable(BitmapDrawable(resources, bitmap)))
    }

    @Test
    fun undecodableDataIsNotUsable() {
        val decoded: Bitmap? = BitmapFactory.decodeByteArray(byteArrayOf(1, 2, 3), 0, 3)
        assertNull(TSAvatarImage.usable(BitmapDrawable(resources, decoded)))
    }

    @Test
    fun aRecycledBitmapIsNotUsable() {
        val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888).apply { recycle() }
        assertNull(TSAvatarImage.usable(BitmapDrawable(resources, bitmap)))
    }

    @Test
    fun aZeroSizeDrawableIsNotUsable() {
        assertNull(TSAvatarImage.usable(GradientDrawable().apply { setSize(0, 0) }))
    }

    @Test
    fun aDrawableWithoutIntrinsicSizeIsUsable() {
        assertNotNull(TSAvatarImage.usable(ColorDrawable(0xFF00FF00.toInt())))
    }

    @Test
    fun usableSizeRejectsEmptyPainterSizes() {
        assertTrue(TSAvatarImage.usableSize(10f, 10f))
        assertTrue(TSAvatarImage.usableSize(Float.NaN, Float.NaN))
        assertFalse(TSAvatarImage.usableSize(0f, 10f))
        assertFalse(TSAvatarImage.usableSize(10f, 0f))
    }
}
