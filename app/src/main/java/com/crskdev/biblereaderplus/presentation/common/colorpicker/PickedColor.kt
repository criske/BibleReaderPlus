/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.common.colorpicker

internal data class PickedColor(val intColor: Int, val pickType: Int = SELECT) {

    companion object {
        const val SELECT = 1
        const val CHANNEL_CHANGE = 1
    }

    operator fun invoke(): Int = intColor

    override fun toString(): String = String.format("#%06X", 0xFFFFFF and intColor)
}

class PickChannel(val value: Int, val type: Int, val pickType: Int)

typealias RGBPickChannels = Triple<PickChannel, PickChannel, PickChannel>