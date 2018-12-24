/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.presentation.util.system.getThemeAttribute
import com.crskdev.biblereaderplus.presentation.util.system.prepInflate
import com.crskdev.biblereaderplus.presentation.util.view.BindableViewHolder
import kotlinx.android.synthetic.main.item_book.view.*
import kotlinx.android.synthetic.main.item_chapter.view.*
import kotlinx.android.synthetic.main.item_verset.view.*

/**
 * Created by Cristian Pela on 23.12.2018.
 */
class PagesAdapter(private val inflater: LayoutInflater, private val action: (ReadUI) -> Unit) :
    PagedListAdapter<ReadUI, ReadVH<*>>(
        object : DiffUtil.ItemCallback<ReadUI>() {
            override fun areItemsTheSame(oldItem: ReadUI, newItem: ReadUI): Boolean =
                oldItem.getKey() == newItem.getKey()

            override fun areContentsTheSame(oldItem: ReadUI, newItem: ReadUI): Boolean =
                oldItem == newItem

        }
    ) {

    override fun getItemViewType(position: Int): Int = getItem(position)?.let {
        when (it) {
            is ReadUI.BookUI -> ReadAdapterViewType.BOOK
            is ReadUI.ChapterUI -> ReadAdapterViewType.CHAPTER
            is ReadUI.VersetUI -> ReadAdapterViewType.VERSET
            else -> ReadAdapterViewType.PLACEHOLDER
        }
    } ?: ReadAdapterViewType.PLACEHOLDER

    override fun onBindViewHolder(holder: ReadVH<*>, position: Int) {
        getItem(position)?.let { holder.bind(it) } ?: holder.unbind()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadVH<*> {
        val layout = inflater.prepInflate(parent).let {
            when (viewType) {
                ReadAdapterViewType.BOOK -> it(R.layout.item_book)
                ReadAdapterViewType.CHAPTER -> it(R.layout.item_chapter)
                ReadAdapterViewType.VERSET -> it(R.layout.item_verset)
                else -> it(android.R.layout.simple_list_item_1)
            }
        }
        return when (viewType) {
            ReadAdapterViewType.BOOK -> BookVH(layout, action)
            ReadAdapterViewType.CHAPTER -> ChapterVH(layout, action)
            ReadAdapterViewType.VERSET -> VersetVH(layout, action)
            else -> PlaceHolderVH(layout)
        }
    }

    fun getKeyAt(position: Int): ReadKey? = getItem(position)?.getKey()

}


abstract class ReadVH<R : ReadUI>(v: View, protected val action: (ReadUI) -> Unit) :
    BindableViewHolder<R>(v) {

}

class BookVH(v: View, action: (ReadUI) -> Unit) : ReadVH<ReadUI.BookUI>(v, action) {

    val defaultColor by lazy {
        itemView.context.getThemeAttribute(
            R.attr.background,
            Color.TRANSPARENT
        )
    }

    val selectColor by lazy {
        itemView.context.getThemeAttribute(
            R.attr.colorControlHighlight,
            Color.TRANSPARENT
        )
    }

    init {
        itemView.setOnClickListener { _ ->
            model?.let {
                action(it)
            }
        }
    }


    override fun onBind(model: ReadUI.BookUI) {
        with(itemView) {
            textBook.text = model.name
            setBackgroundColor(if (model.hasScrollPosition()) selectColor else defaultColor)
        }
    }

    override fun unbind() {
        itemView.textBook.text = null
        itemView.setBackgroundColor(defaultColor)
    }
}

class ChapterVH(v: View, action: (ReadUI) -> Unit) : ReadVH<ReadUI.ChapterUI>(v, action) {

    val defaultColor by lazy {
        itemView.context.getThemeAttribute(
            R.attr.background,
            Color.TRANSPARENT
        )
    }

    val selectColor by lazy {
        itemView.context.getThemeAttribute(
            R.attr.colorControlHighlight,
            Color.TRANSPARENT
        )
    }

    init {
        itemView.tag = 1
        itemView.setOnClickListener { _ ->
            model?.let {
                action(it)
            }
        }
    }


    override fun onBind(model: ReadUI.ChapterUI) {
        with(itemView) {
            textChapter.text = model.name
            setBackgroundColor(
                if (model.hasScrollPosition()) selectColor else defaultColor
            )
        }
    }

    override fun unbind() {
        itemView.textChapter.text = null
        itemView.setBackgroundColor(defaultColor)
    }

}

class VersetVH(v: View, action: (ReadUI) -> Unit) : ReadVH<ReadUI.VersetUI>(v, action) {

    override fun onBind(model: ReadUI.VersetUI) {
        with(itemView) {
            textItemFavVerset.text = model.contents
        }
    }

    override fun unbind() {
        itemView.textItemFavVerset.text = null
    }

}

class PlaceHolderVH(v: View) : ReadVH<ReadUI>(v, { _ -> }) {
    override fun onBind(model: ReadUI) {}
}

object ReadAdapterViewType {
    const val PLACEHOLDER = -1
    const val BOOK = 0
    const val CHAPTER = 1
    const val VERSET = 2
}