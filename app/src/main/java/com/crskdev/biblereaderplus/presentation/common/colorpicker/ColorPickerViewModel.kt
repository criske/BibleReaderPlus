/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.common.colorpicker

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.presentation.util.arch.map

internal class ColorPickerViewModel(
    lockedColors: Set<PickedColor>
) : ViewModel() {

    companion object {
        const val R = 0
        const val G = 1
        const val B = 2
        private val DEFAULT_PICKED = PickedColor(Color.BLACK)
    }

    val lockedColorsLiveData: LiveData<Set<PickedColor>> =
        MutableLiveData<Set<PickedColor>>().apply {
            value = lockedColors
        }

    val selectedColorLiveData: LiveData<PickedColor> = MutableLiveData<PickedColor>()
        .apply {
            value = DEFAULT_PICKED
        }

    val channelLiveData: LiveData<RGBPickChannels> = selectedColorLiveData.map {
        Triple(
            PickChannel(Color.red(it()), ColorPickerViewModel.R, it.pickType),
            PickChannel(Color.green(it()), ColorPickerViewModel.G, it.pickType),
            PickChannel(Color.blue(it()), ColorPickerViewModel.B, it.pickType)
        )
    }

    fun setChannelValue(value: Int, channel: Int) {
        val prevColor = (selectedColorLiveData.value ?: DEFAULT_PICKED)()
        val rgb = mutableListOf(
            Color.red(prevColor),
            Color.green(prevColor),
            Color.blue(prevColor)
        ).apply {
            this[channel] = value
        }
        selectedColorLiveData.cast<MutableLiveData<PickedColor>>().value = PickedColor(
            Color.rgb(
                rgb[0],
                rgb[1],
                rgb[2]
            ),
            PickedColor.CHANNEL_CHANGE
        )
    }

    fun lockColor() {
        selectedColorLiveData.value?.let {
            lockedColorsLiveData.cast<MutableLiveData<Set<PickedColor>>>()
                .value = (lockedColorsLiveData.value ?: emptySet()) + it
        }
    }

    fun unlockColor(color: PickedColor) {
        lockedColorsLiveData.cast<MutableLiveData<Set<PickedColor>>>()
            .value = (lockedColorsLiveData.value ?: emptySet()) - color
    }

    fun setSelectedColor(color: PickedColor) {
        selectedColorLiveData.cast<MutableLiveData<PickedColor>>().value = color
    }


}