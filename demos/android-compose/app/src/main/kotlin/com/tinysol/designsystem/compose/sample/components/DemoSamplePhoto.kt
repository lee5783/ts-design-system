package com.tinysol.designsystem.compose.sample.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun rememberDemoSamplePhoto(): Painter = remember { BitmapPainter(demoSamplePhotoBitmap().asImageBitmap()) }

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
