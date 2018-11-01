/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.presentation.util.system.prepInflate
import com.crskdev.biblereaderplus.presentation.util.view.BindableViewHolder
import kotlinx.android.synthetic.main.item_book.view.*
import kotlinx.android.synthetic.main.item_chapter.view.*
import kotlinx.android.synthetic.main.item_verset.view.*

/**
 * Created by Cristian Pela on 01.11.2018.
 */
object ReadAdapterHelper {

    fun getItemViewType(item: ReadUI): Int = when (item) {
        is ReadUI.BookUI -> ReadAdapterViewType.BOOK
        is ReadUI.ChapterUI -> ReadAdapterViewType.CHAPTER
        is ReadUI.VersetUI -> ReadAdapterViewType.VERSET
    }

    fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int,
        action: (ReadUI) -> Unit
    ): ReadVH<*> =
        inflater.prepInflate(parent).let {
            when (viewType) {
                ReadAdapterViewType.BOOK -> BookVH(it(R.layout.item_book), action)
                ReadAdapterViewType.CHAPTER -> ChapterVH(it(R.layout.item_chapter), action)
                ReadAdapterViewType.VERSET -> VersetVH(it(R.layout.item_verset), action)
                else -> throw IllegalArgumentException("Unknown viewType $viewType")
            }
        }

    fun onBindViewHolder(holder: BindableViewHolder<out ReadUI>, item: ReadUI) {
        holder.bind(item)
    }


}

abstract class ReadVH<R : ReadUI>(v: View, protected val action: (ReadUI) -> Unit) : BindableViewHolder<R>(v)

class BookVH(v: View, action: (ReadUI) -> Unit) : ReadVH<ReadUI.BookUI>(v, action) {

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
        }
    }
}

class ChapterVH(v: View, action: (ReadUI) -> Unit) : ReadVH<ReadUI.ChapterUI>(v, action) {

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
        }
    }

}

class VersetVH(v: View, action: (ReadUI) -> Unit) : ReadVH<ReadUI.VersetUI>(v, action) {

    override fun onBind(model: ReadUI.VersetUI) {
        with(itemView) {
            textVerset.text = model.contents
        }
    }

}

object ReadAdapterViewType {
    const val BOOK = 0
    const val CHAPTER = 1
    const val VERSET = 2
}