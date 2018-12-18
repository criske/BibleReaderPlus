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

    val rChannelLiveData: LiveData<Int> = selectedColorLiveData.map {
        Color.red(it())
    }

    val gChannelLiveData: LiveData<Int> = selectedColorLiveData.map {
        Color.green(it())
    }

    val bChannelLiveData: LiveData<Int> = selectedColorLiveData.map {
        Color.blue(it())
    }

    fun setChannelValue(value: Int, channel: Int) {
        val prevColor = selectedColorLiveData.value ?: DEFAULT_PICKED
        val rgb = mutableListOf(
            Color.red(prevColor()),
            Color.green(prevColor()),
            Color.blue(prevColor())
        ).apply {
            this[channel] = value
        }
        selectedColorLiveData.cast<MutableLiveData<PickedColor>>().value = PickedColor(
            Color.rgb(
                rgb[0],
                rgb[1],
                rgb[2]
            )
        )
    }

    fun restore(selectedColor: PickedColor) {
        selectedColorLiveData.cast<MutableLiveData<PickedColor>>().value = selectedColor
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