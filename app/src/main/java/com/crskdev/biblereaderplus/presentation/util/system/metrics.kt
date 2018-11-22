/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.system

import android.content.res.Resources
import android.util.TypedValue
import kotlin.math.roundToInt

/**
 * Created by Cristian Pela on 01.11.2018.
 */
fun Float.dpToPx(resources: Resources): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics).roundToInt()


fun Int.dpToPx(resources: Resources): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), resources.displayMetrics).roundToInt()

