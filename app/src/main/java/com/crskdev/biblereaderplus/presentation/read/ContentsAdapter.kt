/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.crskdev.biblereaderplus.presentation.util.view.BindableViewHolder

/**
 * Created by Cristian Pela on 22.12.2018.
 */
class ContentsAdapter(private val inflater: LayoutInflater,
                      private val action: (Action, ReadUI.ContentUI) -> Unit) :
    ListAdapter<ReadUI.ContentUI, ContentVH>(
        object : DiffUtil.ItemCallback<ReadUI.ContentUI>() {
            override fun areItemsTheSame(oldItem: ReadUI.ContentUI, newItem: ReadUI.ContentUI): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ReadUI.ContentUI, newItem: ReadUI.ContentUI): Boolean =
                oldItem == newItem

        }) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is ReadUI.BookUI -> Type.BOOK
            is ReadUI.ChapterUI -> Type.CHAPTER
            else -> throw IllegalStateException("Content must be BookUI or ChapterUI")
        }

    override fun onBindViewHolder(holder: ContentVH, position: Int) =
        holder.bind(getItem(position))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentVH =
        ContentVH(
            inflater
                .inflate(android.R.layout.simple_list_item_1, parent, false),
            viewType,
            action
        )

    override fun onViewRecycled(holder: ContentVH) {
        holder.unbind()
    }

    object Type {
        const val BOOK = 0
        const val CHAPTER = 1
    }

    enum class Action {
        SELECT, EXPAND
    }
}

@Suppress("RedundantLambdaArrow")
class ContentVH(v: View, val type: Int, private val action: (ContentsAdapter.Action, ReadUI.ContentUI) -> Unit) :
    BindableViewHolder<ReadUI.ContentUI>(v) {

    init {
        itemView.setOnClickListener { _ ->
            model?.let {
                action(ContentsAdapter.Action.SELECT, it)
            }
        }
        itemView.setOnLongClickListener { _ ->
            if (type == ContentsAdapter.Type.BOOK) {
                model?.let {
                    action(ContentsAdapter.Action.EXPAND, it.setExpanded(!it.isExpanded))
                }
                true
            } else
                false
        }

    }


    override fun onBind(model: ReadUI.ContentUI) {
        with(itemView as TextView) {
            text = model.name + " " + model.isExpanded()
            if (model.hasScrollPosition()) {
                setBackgroundColor(Color.LTGRAY)
            }
            tag = type
        }
    }

    override fun unbind() {
        itemView.setBackgroundColor(Color.TRANSPARENT)
        itemView.tag = null
    }
}