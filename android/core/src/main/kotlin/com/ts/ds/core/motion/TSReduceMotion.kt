package com.ts.ds.core.motion

import android.animation.ValueAnimator

public object TSReduceMotion {
    public val isEnabled: Boolean
        get() = !ValueAnimator.areAnimatorsEnabled()

    public fun durationMillis(durationMillis: Int, reduceMotion: Boolean): Int = if (reduceMotion) 0 else durationMillis
}
