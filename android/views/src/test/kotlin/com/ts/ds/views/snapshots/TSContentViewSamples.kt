package com.ts.ds.views.snapshots

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSAvatarSize
import com.ts.ds.core.style.TSBadgeTone
import com.ts.ds.views.components.avatar.TSAvatarView
import com.ts.ds.views.components.badge.TSBadgeView

private const val SAMPLE_LONG_BADGE = "Awaiting review from the platform team"

private val samplePeople = listOf("Ada Lovelace", "Wei Ming", "山田")

private fun samplePhotoBitmap(): Bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).also { bitmap ->
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.shader = LinearGradient(0f, 0f, 64f, 64f, 0xFF7C3AED.toInt(), 0xFFF59E0B.toInt(), Shader.TileMode.CLAMP)
    canvas.drawRect(0f, 0f, 64f, 64f, paint)
    paint.shader = null
    paint.color = 0xFFFDE68A.toInt()
    canvas.drawCircle(32f, 24f, 12f, paint)
    canvas.drawCircle(32f, 70f, 28f, paint)
}

private fun Context.centeredRow(): LinearLayout = galleryRow().apply { gravity = Gravity.CENTER_VERTICAL }

internal fun badgeGallerySample(context: Context): View = context.galleryColumn().apply {
    TSBadgeTone.entries.forEach { tone ->
        addView(
            context.centeredRow().apply {
                addView(TSBadgeView(context).apply { text = tone.name; this.tone = tone })
                addView(TSBadgeView(context).apply { text = tone.name; this.tone = tone; showsDot = true })
                addView(TSBadgeView(context).apply { text = tone.name; this.tone = tone; icon = TSIcon.Star })
            },
        )
    }
    addView(
        TSBadgeView(context).apply {
            text = SAMPLE_LONG_BADGE
            tone = TSBadgeTone.Warning
            showsDot = true
        },
        LinearLayout.LayoutParams(context.dp(160f), ViewGroup.LayoutParams.WRAP_CONTENT),
    )
}

internal fun avatarGallerySample(context: Context): View = context.galleryColumn().apply {
    val photo = samplePhotoBitmap()
    TSAvatarSize.entries.forEach { size ->
        addView(
            context.centeredRow().apply {
                samplePeople.forEach { person -> addView(TSAvatarView(context).apply { name = person; this.size = size }) }
                addView(
                    TSAvatarView(context).apply {
                        name = samplePeople.first()
                        image = BitmapDrawable(context.resources, photo)
                        this.size = size
                    },
                )
                addView(TSAvatarView(context).apply { this.size = size })
            },
        )
    }
}
