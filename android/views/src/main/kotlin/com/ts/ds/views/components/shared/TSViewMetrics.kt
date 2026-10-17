package com.ts.ds.views.components.shared

import android.view.View
import androidx.annotation.Px
import kotlin.math.roundToInt

@Px
internal fun View.dpToPx(dp: Float): Int = (dp * resources.displayMetrics.density).roundToInt()

@Px
internal fun expandToTouchTarget(@Px visual: Int, measureSpec: Int, @Px minTouchTarget: Int): Int =
    when (View.MeasureSpec.getMode(measureSpec)) {
        View.MeasureSpec.EXACTLY -> visual
        View.MeasureSpec.AT_MOST -> maxOf(visual, minTouchTarget).coerceAtMost(View.MeasureSpec.getSize(measureSpec))
        else -> maxOf(visual, minTouchTarget)
    }
