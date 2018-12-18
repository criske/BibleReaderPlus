/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

@file:Suppress("unused")

package com.crskdev.biblereaderplus.presentation.common.colorpicker

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.DecelerateInterpolator
import android.widget.SeekBar
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorFilter
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.presentation.util.arch.ViewLifecycleOwner
import com.crskdev.biblereaderplus.presentation.util.system.getColorCompat
import com.crskdev.biblereaderplus.presentation.util.view.ColorUtilsExtra
import kotlinx.android.synthetic.main.color_picker_layout.view.*

/**
 * Just a simple color picker
 */
class ColorPickerView : ConstraintLayout {

    companion object {
        private const val KEY_LOCKED_COLORS = "KEY_LOCKED_COLORS"
        private const val ANIMATION_DURATION_MS = 400L
    }

    private var colorPickListener: ((String) -> Unit)? = null

    private lateinit var viewModel: ColorPickerViewModel

    private val lifecycleOwner = ViewLifecycleOwner(this)

    private val storage by lazy {
        context.getSharedPreferences("color_picker_locked_colors", Context.MODE_PRIVATE)
    }

    private val accentColor by lazy { context.getColorCompat(R.color.secondaryColor) }

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
        viewModel = ColorPickerViewModel(storage.getStringSet(KEY_LOCKED_COLORS, emptySet())!!.map {
            PickedColor(it.toInt())
        }.toSet())
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
            colorPickerRecylerLocked.smoothScrollToPosition(it.size - 1)
        })

        viewModel.selectedColorLiveData.observe(lifecycleOwner, Observer {
            val contrastColor = ColorUtilsExtra.defaultContrastColor(it.intColor)
//            if (it.pickType == PickedColor.SELECT) {
//                ObjectAnimator.ofInt(
//                    colorPickerSelectedColorView, "cardBackgroundColor",
//                    it.intColor
//                ).apply {
//                    setEvaluator(ArgbEvaluator())
//                    interpolator = LinearInterpolator()
//                    duration = ANIMATION_DURATION_MS * 2
//                }.start()
//            } else {
//                colorPickerSelectedColorView.cast<CardView>().setCardBackgroundColor(it.intColor)
//            }
            colorPickerSelectedColorView.cast<CardView>().setCardBackgroundColor(it.intColor)
            with(colorPickerSelectedColorText) {
                setTextColor(contrastColor)
                text = it.toString()
            }
            colorPickerImgLock.setColorFilter(contrastColor)

        })
        viewModel.channelLiveData.observe(lifecycleOwner, Observer {
            setSeekBarsProgress(it)
            decorateSeekBars(it)
        })
        registerSeekListener(colorPickerSeekR, ColorPickerViewModel.R)
        registerSeekListener(colorPickerSeekG, ColorPickerViewModel.G)
        registerSeekListener(colorPickerSeekB, ColorPickerViewModel.B)
    }

    private fun setSeekBarsProgress(rgbChannels: RGBPickChannels) {
        val type = rgbChannels.first.pickType
        if (type == PickedColor.SELECT) {
            val rAnim = ObjectAnimator
                .ofInt(colorPickerSeekR, "progress", rgbChannels.first.value)
            val gAnim = ObjectAnimator
                .ofInt(colorPickerSeekG, "progress", rgbChannels.second.value)
            val bAnim = ObjectAnimator
                .ofInt(colorPickerSeekB, "progress", rgbChannels.third.value)
            AnimatorSet().apply {
                interpolator = DecelerateInterpolator()
                duration = ANIMATION_DURATION_MS
                playTogether(rAnim, gAnim, bAnim)
            }.start()
        } else {
            colorPickerSeekR.progress = rgbChannels.first.value
            colorPickerSeekG.progress = rgbChannels.second.value
            colorPickerSeekB.progress = rgbChannels.third.value
        }
    }

    private fun decorateSeekBars(rgbChannels: RGBPickChannels) {
        decorateSeekBar(colorPickerSeekR, accentColor, rgbChannels.first)
        decorateSeekBar(colorPickerSeekG, accentColor, rgbChannels.second)
        decorateSeekBar(colorPickerSeekB, accentColor, rgbChannels.third)
    }

    private fun decorateSeekBar(seekBar: SeekBar, foregroundColor: Int, pickChannel: PickChannel) {
        val color = arrayOf(0, 0, 0).apply {
            this[pickChannel.type] = pickChannel.value
        }.let { Color.rgb(it[0], it[1], it[2]) }
        with(seekBar) {
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

    override fun onDetachedFromWindow() {
        storage.edit {
            putStringSet(
                KEY_LOCKED_COLORS,
                viewModel.lockedColorsLiveData.value?.map { it.intColor.toString() }?.toSet()
            )
        }
        super.onDetachedFromWindow()
    }

    override fun onSaveInstanceState(): Parcelable? {
        return bundleOf(
            State.STATE_KEY to State(
                super.onSaveInstanceState(),
                viewModel.selectedColorLiveData.value!!.intColor
            )
        )
    }

    override fun onRestoreInstanceState(restoredState: Parcelable?) {
        if (restoredState is Bundle?) {
            restoredState?.getParcelable<State>(State.STATE_KEY)?.let {
                viewModel.setSelectedColor(PickedColor(it.color))
                super.onRestoreInstanceState(it.superState)
            }
        }
        super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE)
    }

    fun setOnColorPickListener(colorPickListener: (String) -> Unit) {
        this.colorPickListener = colorPickListener
    }

    fun setSelectedColor(color: String) =
        viewModel.setSelectedColor(PickedColor(Color.parseColor(color)))

    fun getPickedColor(): String = viewModel.selectedColorLiveData.value!!.toString()

    internal class State(superState: Parcelable?,
                         val color: Int) : BaseSavedState(superState) {
        companion object {
            const val STATE_KEY = "ColorPickerState_Key"
        }
    }

}


