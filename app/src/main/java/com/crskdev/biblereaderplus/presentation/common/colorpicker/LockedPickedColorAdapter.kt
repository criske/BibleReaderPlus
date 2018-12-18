/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.common.colorpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.presentation.util.view.BindableViewHolder
import com.crskdev.biblereaderplus.presentation.util.view.ColorUtilsExtra
import kotlinx.android.synthetic.main.color_picker_item_locked.view.*

/**
 * Created by Cristian Pela on 17.12.2018.
 */
internal class LockedPickedColorAdapter(
    private val inflater: LayoutInflater,
    private val action: (PickedColor, Action) -> Unit) : ListAdapter<PickedColor, PickedColorVH>(
    object : DiffUtil.ItemCallback<PickedColor>() {
        override fun areItemsTheSame(oldItem: PickedColor, newItem: PickedColor): Boolean =
            oldItem.intColor == newItem.intColor

        override fun areContentsTheSame(oldItem: PickedColor, newItem: PickedColor): Boolean =
            oldItem == newItem

    }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickedColorVH =
        PickedColorVH(
            inflater.inflate(R.layout.color_picker_item_locked, parent, false),
            action
        )

    override fun onBindViewHolder(holder: PickedColorVH, position: Int) {
        holder.bind(getItem(position))
    }

    enum class Action {
        SELECT, REMOVE
    }

}

internal class PickedColorVH(view: View,
                             private val action: (PickedColor, LockedPickedColorAdapter.Action) -> Unit) :
    BindableViewHolder<PickedColor>(view) {

    init {
        with(itemView) {
            colorPickerLockedColorBtnRemove.setOnClickListener {
                model?.let {
                    action(it, LockedPickedColorAdapter.Action.REMOVE)
                }
            }
            setOnClickListener {
                model?.let {
                    action(it, LockedPickedColorAdapter.Action.SELECT)
                }
            }
        }
    }

    override fun onBind(model: PickedColor) {
        with(itemView as CardView) {
            val contrastColor = ColorUtilsExtra.defaultContrastColor(model.intColor)
            setCardBackgroundColor(model.intColor)
            colorPickerLockedColorBtnRemove.setColorFilter(contrastColor)
        }
    }

}