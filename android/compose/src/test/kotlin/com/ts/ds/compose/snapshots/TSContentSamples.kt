package com.ts.ds.compose.snapshots

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.avatar.TSAvatar
import com.ts.ds.compose.components.badge.TSBadge
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSAvatarSize
import com.ts.ds.core.style.TSBadgeTone

internal const val SAMPLE_LONG_BADGE = "Awaiting review from the platform team"

internal val samplePeople = listOf("Ada Lovelace", "Wei Ming", "山田")

internal fun samplePhotoBitmap(): Bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).also { bitmap ->
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.shader = LinearGradient(0f, 0f, 64f, 64f, 0xFF7C3AED.toInt(), 0xFFF59E0B.toInt(), Shader.TileMode.CLAMP)
    canvas.drawRect(0f, 0f, 64f, 64f, paint)
    paint.shader = null
    paint.color = 0xFFFDE68A.toInt()
    canvas.drawCircle(32f, 24f, 12f, paint)
    canvas.drawCircle(32f, 70f, 28f, paint)
}

private fun samplePhoto(): Painter = BitmapPainter(samplePhotoBitmap().asImageBitmap())

@Composable
internal fun BadgeGallerySample() {
    SampleColumn {
        TSBadgeTone.entries.forEach { tone ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TSBadge(tone.name, tone = tone)
                TSBadge(tone.name, tone = tone, showsDot = true)
                TSBadge(tone.name, tone = tone, icon = TSIcon.Star)
            }
        }
        TSBadge(SAMPLE_LONG_BADGE, modifier = Modifier.width(160.dp), tone = TSBadgeTone.Warning, showsDot = true)
    }
}

@Composable
internal fun AvatarGallerySample() {
    val photo = samplePhoto()
    SampleColumn {
        TSAvatarSize.entries.forEach { size ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                samplePeople.forEach { TSAvatar(it, size = size) }
                TSAvatar(samplePeople.first(), image = photo, size = size)
                TSAvatar("", size = size)
            }
        }
    }
}
