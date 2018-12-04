/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.presentation.util.system.getColorCompat
import com.crskdev.biblereaderplus.presentation.util.view.BindableViewHolder
import com.google.android.material.chip.Chip

/**
 * Created by Cristian Pela on 29.11.2018.
 */
class TagsAdapter(private val inflater: LayoutInflater,
                  private val tagBehaviour: TagBehaviour = TagBehaviour(),
                  private val action: (Tag, TagSelectAction) -> Unit) :

    ListAdapter<Tag, TagVH>(object : DiffUtil.ItemCallback<Tag>() {
        override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean = oldItem == newItem
    }) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagVH =
        TagVH(inflater.inflate(R.layout.item_tag, parent, false), tagBehaviour, action)

    override fun onBindViewHolder(holder: TagVH, position: Int) =
        holder.bind(getItem(position))

}

class TagBehaviour(val selectPolicy: SelectPolicy = SelectPolicy.TAP_DISABLED, val isClosable: Boolean = false) {

    enum class SelectPolicy {
        TAP_DISABLED,
        SELECT_ON_TAP,
        CONTEXT_MENU_ON_TAP,
        SELECT_ON_TAP_CONTENT_MENU_ON_LONG_TAP,
        CONTEXT_MENU_ON_TAP_SELECT_ON_LONG_TAP
    }
}

enum class TagSelectAction {
    CLOSE, CONTEXT_MENU_RENAME, CONTEXT_MENU_DELETE, CONTEXT_MENU_CHANGE_COLOR, SELECT
}

class TagVH(v: View, private val tagBehaviour: TagBehaviour, private val action: (Tag, TagSelectAction) -> Unit) :
    BindableViewHolder<Tag>(v) {


    init {
        with(itemView.cast<Chip>()) {
            if (tagBehaviour.isClosable) {
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    model?.let { action(it, TagSelectAction.CLOSE) }
                }
            }
            when (tagBehaviour.selectPolicy) {
                TagBehaviour.SelectPolicy.SELECT_ON_TAP -> {
                    setOnClickListener {
                        model?.let { action(it, TagSelectAction.SELECT) }
                    }
                }
                TagBehaviour.SelectPolicy.CONTEXT_MENU_ON_TAP -> {
                    setOnClickListener {
                        model?.let { action(it, TagSelectAction.CONTEXT_MENU_RENAME) }
                    }
                }
                TagBehaviour.SelectPolicy.SELECT_ON_TAP_CONTENT_MENU_ON_LONG_TAP -> {
                    setOnClickListener {
                        model?.let { action(it, TagSelectAction.SELECT) }
                    }
                    setOnLongClickListener {
                        model?.let { action(it, TagSelectAction.CONTEXT_MENU_RENAME) }
                        true
                    }
                }
                TagBehaviour.SelectPolicy.CONTEXT_MENU_ON_TAP_SELECT_ON_LONG_TAP -> {
                    setOnClickListener {
                        model?.let { action(it, TagSelectAction.CONTEXT_MENU_RENAME) }
                    }
                    setOnLongClickListener {
                        model?.let { action(it, TagSelectAction.SELECT) }
                        true
                    }
                }
                else -> Unit
            }
        }
    }

    override fun onBind(model: Tag) {
        with(itemView.cast<Chip>()) {
            text = model.name
            setContrastingTextColor(Color.parseColor(model.color))
        }
    }


    private fun showContextMenu() {
        itemView.setOnCreateContextMenuListener { menu, view, info ->


        }
    }

}

fun Chip.setContrastingTextColor(@ColorInt color: Int) {
    chipBackgroundColor = ColorStateList.valueOf(color)
    chipStrokeColor = ColorStateList.valueOf(ColorUtils.blendARGB(Color.WHITE, color, 0.5f))
    setTextColor(
        ColorUtils.calculateLuminance(color).takeIf { it < 0.5 }?.let {
            context.getColorCompat(R.color.secondaryTextColor)
        } ?: context.getColorCompat(R.color.primaryTextColor)
    )
}