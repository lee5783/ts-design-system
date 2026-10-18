package com.tinysol.designsystem.views.theme

import android.content.Context
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.google.android.material.color.MaterialColors

@ColorInt
public fun Context.tsColor(@AttrRes attr: Int): Int = MaterialColors.getColor(this, attr, javaClass.simpleName)

@ColorInt
public fun View.tsColor(@AttrRes attr: Int): Int = MaterialColors.getColor(this, attr)
