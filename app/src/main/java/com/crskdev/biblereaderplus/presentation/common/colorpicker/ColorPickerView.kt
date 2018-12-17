/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.common.colorpicker

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorFilter
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.presentation.util.arch.ViewLifecycleOwner
import com.crskdev.biblereaderplus.presentation.util.system.getColorCompat
import kotlinx.android.synthetic.main.color_picker_layout.view.*

/**
 * Just a simple color picker
 */
class ColorPickerView : ConstraintLayout {

    private var colorPickListener: ((Int) -> Unit)? = null

    private var viewModel: ColorPickerViewModel = ColorPickerViewModel()

    private val lifecycleOwner = ViewLifecycleOwner(this)

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.color_picker_layout, this, true)
        observeViewModel()
    }

    //@formatter:off
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        LayoutInflater.from(context).inflate(R.layout.color_picker_layout, this, true)
        observeViewModel()
    }
    //@formatter:on

    private fun observeViewModel() {
        val lockedAdapter =
            LockedPickedColorAdapter(LayoutInflater.from(context)) { picked, action ->
                when (action) {
                    LockedPickedColorAdapter.Action.SELECT -> viewModel.setSelectedColor(picked)
                    LockedPickedColorAdapter.Action.REMOVE -> viewModel.unlockColor(picked)
                }
            }

        with(colorPickerRecylerLocked) {
            adapter = lockedAdapter
        }

        colorPickerImgLock.setOnClickListener {
            viewModel.lockColor()
        }

        colorPickerSelectedColorView.setOnClickListener {
            colorPickListener?.invoke(getPickedColor())
        }

        viewModel.lockedColorsLiveData.observe(lifecycleOwner, Observer {
            lockedAdapter.submitList(it.toList())
        })

        viewModel.selectedColorLiveData.observe(lifecycleOwner, Observer {
            val contrastColor = ColorUtils.calculateLuminance(it.intColor).let {
                if (it < 0.5) {
                    Color.WHITE
                } else {
                    Color.DKGRAY
                }
            }
            colorPickerSelectedColorView.cast<CardView>().setCardBackgroundColor(it.intColor)
            with(colorPickerSelectedColorText) {
                setTextColor(contrastColor)
                text = it.toString()
            }
            colorPickerImgLock.setColorFilter(contrastColor)

        })
        val accentColor = context.getColorCompat(R.color.secondaryColor)
        viewModel.rChannelLiveData.observe(lifecycleOwner, Observer {
            decorateSeekBar(colorPickerSeekR, accentColor, it, ColorPickerViewModel.R)
        })
        viewModel.gChannelLiveData.observe(lifecycleOwner, Observer {
            decorateSeekBar(colorPickerSeekG, accentColor, it, ColorPickerViewModel.G)
        })
        viewModel.bChannelLiveData.observe(lifecycleOwner, Observer {
            decorateSeekBar(colorPickerSeekB, accentColor, it, ColorPickerViewModel.B)
        })
        registerSeekListener(colorPickerSeekR, ColorPickerViewModel.R)
        registerSeekListener(colorPickerSeekG, ColorPickerViewModel.G)
        registerSeekListener(colorPickerSeekB, ColorPickerViewModel.B)
    }

    private fun decorateSeekBar(seekBar: SeekBar, foregroundColor: Int, channelColor: Int, channel: Int) {
        val color = arrayOf(0, 0, 0).apply {
            this[channel] = channelColor
        }.let { Color.rgb(it[0], it[1], it[2]) }
        with(seekBar) {
            progress = channelColor
            thumb.colorFilter = PorterDuff.Mode.SRC_ATOP.toColorFilter(
                ColorUtils.compositeColors(
                    ColorUtils.setAlphaComponent(color, 50),
                    foregroundColor
                )
            )
            progressDrawable.colorFilter = PorterDuff.Mode.SRC_ATOP.toColorFilter(color)
        }
    }

    private fun registerSeekListener(seekBar: SeekBar, channel: Int) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setChannelValue(progress, channel)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })
    }

    override fun onSaveInstanceState(): Parcelable? {
        return bundleOf(
            State.STATE_KEY to State.serialized(
                super.onSaveInstanceState(),
                viewModel.selectedColorLiveData.value!!.intColor,
                viewModel.lockedColorsLiveData.value!!.map { it.intColor }
            )
        )
    }

    override fun onRestoreInstanceState(restoredState: Parcelable?) {
        if (restoredState is Bundle?) {
            restoredState?.getParcelable<State>(State.STATE_KEY)?.let {
                viewModel.restore(it.color, it.deserializeLockedColors())
                super.onRestoreInstanceState(it.superState)
            }
        }
        super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE)
    }

    fun setOnColorPickListener(colorPickListener: (Int) -> Unit) {
        this.colorPickListener = colorPickListener
    }

    fun getPickedColor(): Int = viewModel.selectedColorLiveData.value!!.intColor

    internal class State(superState: Parcelable?,
                         val color: Int,
                         val lockedColors: String) : BaseSavedState(superState) {

        companion object {
            const val STATE_KEY = "ColorPickerState_Key"
            private const val DELIM = ";"
            fun serialized(superState: Parcelable?,
                           selectedColor: Int,
                           lockedColors: List<Int>): State =
                State(superState, selectedColor, buildString {
                    lockedColors.forEachIndexed { i, c ->
                        val d = if (i < lockedColors.size - 1) DELIM else ""
                        append("$c$d")
                    }
                })
        }

        fun deserializeLockedColors(): List<Int> = mutableListOf<Int>().apply {
            lockedColors.split(DELIM).forEach {
                add(it.toInt())
            }
        }
    }

}


