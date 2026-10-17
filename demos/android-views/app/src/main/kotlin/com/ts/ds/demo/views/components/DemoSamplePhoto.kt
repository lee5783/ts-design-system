package com.ts.ds.demo.views.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

fun demoSamplePhoto(context: Context): Drawable = BitmapDrawable(context.resources, demoSamplePhotoBitmap())

private fun demoSamplePhotoBitmap(): Bitmap = Bitmap.createBitmap(PHOTO_SIZE, PHOTO_SIZE, Bitmap.Config.ARGB_8888).also { bitmap ->
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    canvas.drawColor(BACKGROUND)
    paint.color = SKIN
    canvas.drawOval(40f, 22f, 80f, 62f, paint)
    canvas.drawOval(22f, 70f, 98f, 140f, paint)
}

private const val PHOTO_SIZE = 120
private const val BACKGROUND = 0xFF29739E.toInt()
private const val SKIN = 0xFFF5C28C.toInt()
