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
                  private val action: (Tag, TagBehaviour.Action) -> Unit) :

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

class TagBehaviour(val isSelectable: Boolean = false, val isClosable: Boolean = false) {
    enum class Action {
        SELECT, CLOSE
    }
}

class TagVH(v: View, private val tagBehaviour: TagBehaviour, private val action: (Tag, TagBehaviour.Action) -> Unit) :
    BindableViewHolder<Tag>(v) {

    init {
        with(itemView.cast<Chip>()) {
            if (tagBehaviour.isSelectable) {
                setOnClickListener { _ ->
                    model?.let { action(it, TagBehaviour.Action.SELECT) }
                }
            }
            if (tagBehaviour.isClosable) {
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    model?.let { action(it, TagBehaviour.Action.CLOSE) }
                }
            }
        }
    }

    override fun onBind(model: Tag) {
        itemView.cast<Chip>().setFilterTag(model)
    }

}

fun Chip.setFilterTag(tag: Tag) {
    val tagColor = Color.parseColor(tag.color)
    chipBackgroundColor = ColorStateList.valueOf(tagColor)
    text = tag.name
    this.tag = tag
    chipStrokeColor = ColorStateList.valueOf(ColorUtils.compositeColors(Color.WHITE, tagColor))
    setTextColor(
        ColorUtils.calculateLuminance(tagColor).takeIf { it < 0.5 }?.let {
            context.getColorCompat(R.color.secondaryTextColor)
        } ?: context.getColorCompat(R.color.primaryTextColor)
    )
}