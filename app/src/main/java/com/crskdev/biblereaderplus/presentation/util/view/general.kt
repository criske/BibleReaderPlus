/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.view

import android.graphics.Color
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils


/**
 * Created by Cristian Pela on 25.11.2018.
 */
fun View.isPointInside(x: Float, y: Float): Boolean {
    val location = IntArray(2)
    getLocationOnScreen(location)
    val viewX = location[0]
    val viewY = location[1]
    // point is inside view bounds
    return x > viewX && x < viewX + width && y > viewY && y < viewY + height
}


fun View.getDrawingRect(): Rect = Rect().apply {
    getDrawingRect(this)
}

fun View.getHitRect(): Rect = Rect().apply {
    getHitRect(this)
}

object ColorUtilsExtra {

    /**
     * Obtain the light or dark color based on test color and a light threshold
     *
     * When the test color is too dark (<lightThreshold) it will choose the first curry argument (the lightColor)
     * else the darkColor (2nd curry argument)
     */
    fun contrastColor(@ColorInt testColor: Int, lightThreshold: Double = 0.5): (Int, Int) -> Int {
        val lightFactor = ColorUtils.calculateLuminance(testColor)
        return { lightColor, darkColor ->
            if (lightFactor <= lightThreshold) {
                lightColor
            } else {
                darkColor
            }
        }
    }

    fun defaultContrastColor(testColor: Int): Int =
        contrastColor(testColor)(Color.WHITE, Color.DKGRAY)

}