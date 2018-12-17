/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.common.colorpicker

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.presentation.util.arch.ViewLifecycleOwner
import com.crskdev.biblereaderplus.presentation.util.arch.map
import com.crskdev.biblereaderplus.presentation.util.arch.scan

/**
 * Just simple color picker
 */
class ColorPickerView : ConstraintLayout {

    private var colorPickListener: ((Int) -> Unit)? = null

    private lateinit var viewModel: ColorPickerViewModel

    private val lifecycleOwner = ViewLifecycleOwner(this)

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    //@formatter:off
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        LayoutInflater.from(context).inflate(R.layout.color_picker_layout, this, true)
        initViewModel()
    }
    //@formatter:on


    private fun initViewModel() {

    }

    fun setOnColorPickListener(colorPickListener: (Int) -> Unit) {
        this.colorPickListener = colorPickListener
    }

}


internal class ColorPickerViewModel : ViewModel() {

    companion object {
        const val R = 0
        const val G = 1
        const val B = 2
    }

    private val selectionChannel: MutableLiveData<Pair<Int, Int>> = MutableLiveData()

    val selectedColorLiveData: LiveData<Int> = selectionChannel.scan(0) { acc, curr ->
        val channel = curr.second
        val channelVal = curr.first
        val rgb = arrayOf(Color.red(acc), Color.green(acc), Color.blue(acc))
            .apply {
                this[channel] = channelVal
            }
        Color.rgb(rgb[0], rgb[1], rgb[2])
    }

    val rChannelLiveData: LiveData<Int> = selectedColorLiveData.map {
        Color.red(it)
    }

    val gChannelLiveData: LiveData<Int> = selectedColorLiveData.map {
        Color.green(it)
    }

    val bChannelLiveData: LiveData<Int> = selectedColorLiveData.map {
        Color.blue(it)
    }

    fun setChannelValue(value: Int, channel: Int) {
        selectionChannel.value = value to channel
    }


}
