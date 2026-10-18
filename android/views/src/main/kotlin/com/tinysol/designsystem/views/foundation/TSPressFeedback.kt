package com.tinysol.designsystem.views.foundation

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.content.res.ColorStateList
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.tinysol.designsystem.tokens.R as TokensR

public fun View.applyPressedAlpha(
    pressedAlpha: Float = ResourcesCompat.getFloat(resources, TokensR.dimen.ts_opacity_pressed),
) {
    val duration = resources.getInteger(TokensR.integer.ts_duration_fast).toLong()
    stateListAnimator = StateListAnimator().apply {
        addState(intArrayOf(android.R.attr.state_pressed), alphaAnimator(pressedAlpha, duration))
        addState(intArrayOf(), alphaAnimator(1f, duration))
    }
}

private fun View.alphaAnimator(target: Float, duration: Long): ObjectAnimator =
    ObjectAnimator.ofFloat(this, View.ALPHA, target).setDuration(duration)

public fun pressableColorStateList(
    @ColorInt normal: Int,
    @ColorInt pressed: Int,
    @ColorInt disabled: Int = normal,
): ColorStateList = ColorStateList(
    arrayOf(
        intArrayOf(-android.R.attr.state_enabled),
        intArrayOf(android.R.attr.state_pressed),
        intArrayOf(),
    ),
    intArrayOf(disabled, pressed, normal),
)
