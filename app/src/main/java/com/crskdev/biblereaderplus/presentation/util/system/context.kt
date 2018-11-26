/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.system

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

/**
 * Created by Cristian Pela on 02.11.2018.
 */
inline fun <reified T> Context.getThemeAttribute(id: Int, default: T): T = TypedValue().let {
    theme.resolveAttribute(id, it, true)
    it.data as T
}


fun Drawable.tint(context: Context, @ColorRes color: Int = android.R.color.darker_gray) =
    DrawableCompat.setTint(
        DrawableCompat.wrap(this).mutate(),
        ContextCompat.getColor(context, color)
    )

fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable? = ContextCompat.getDrawable(this, id)

@ColorInt
fun Context.getColorCompat(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)

fun Int.colorResToInt(context: Context) = context.getColorCompat(this)
