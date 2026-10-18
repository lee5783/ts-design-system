package com.tinysol.designsystem.core.logic

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

public object TSAvatarImage {
    public fun usable(drawable: Drawable?): Drawable? = drawable?.takeIf { it.hasPixels() }

    public fun usableSize(width: Float, height: Float): Boolean =
        width.isNaN() || height.isNaN() || (width > 0f && height > 0f)

    private fun Drawable.hasPixels(): Boolean {
        if (this is BitmapDrawable) {
            val bitmap = bitmap ?: return false
            return !bitmap.isRecycled && bitmap.width > 0 && bitmap.height > 0
        }
        if (intrinsicWidth < 0 || intrinsicHeight < 0) return true
        return intrinsicWidth > 0 && intrinsicHeight > 0
    }
}
