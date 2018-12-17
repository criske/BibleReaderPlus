/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.common.colorpicker

internal data class PickedColor(val intColor: Int) {
    operator fun invoke(): Int = intColor
    override fun toString(): String = String.format("#%06X", 0xFFFFFF and intColor)
}