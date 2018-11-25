/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.view

import android.view.View


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