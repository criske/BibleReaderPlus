/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import kotlinx.android.synthetic.main.item_verset.view.*

/**
 * Created by Cristian Pela on 22.11.2018.
 */
class FavoriteVersetsAdapter(private val inflater: LayoutInflater,
                             private val action: (FavoriteAction) -> Unit) :

    PagedListAdapter<Read.Verset, FavoriteVersetVH>(object : DiffUtil.ItemCallback<Read.Verset>() {
        override fun areItemsTheSame(oldItem: Read.Verset, newItem: Read.Verset): Boolean =
            oldItem.key == newItem.key

        override fun areContentsTheSame(oldItem: Read.Verset, newItem: Read.Verset): Boolean =
            oldItem == newItem
    }) {

    lateinit var selectionTracker: SelectionTracker<Long>

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long =
        getItem(position)?.key?.id?.toLong() ?: RecyclerView.NO_ID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteVersetVH =
        FavoriteVersetVH(
            inflater.inflate(R.layout.item_verset, parent, false),
            selectionTracker,
            action
        )


    override fun onBindViewHolder(holder: FavoriteVersetVH, position: Int) {
        val key = getItemId(position)
        holder.bind(getItem(position), position, key)
    }

    override fun onViewRecycled(holder: FavoriteVersetVH) {
        holder.clear()
    }
}


class FavoriteVersetVH(view: View,
                       private val selectionTracker: SelectionTracker<Long>,
                       private val action: (FavoriteAction) -> Unit) :
    RecyclerView.ViewHolder(view) {

    private var verset: Read.Verset? = null

    internal var itemDetails: VersetItemDetails =
        VersetItemDetails(RecyclerView.NO_POSITION, RecyclerView.NO_ID, Rect())

    private var isVisualSelected = false

    init {
        with(itemView.cast<MotionLayout>()) {
            textItemFavVerset.getHitRect(itemDetails.selectionRect)
            setTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) =
                    Unit

                override fun onTransitionCompleted(layput: MotionLayout, id: Int) {
                    itemDetails.key?.let {
                        isVisualSelected = id == R.id.end
                        if (isVisualSelected) {
                            selectionTracker.select(it)
                        } else {
                            selectionTracker.deselect(it)
                        }
                    }
                    //recalc rect
                    textItemFavVerset.getHitRect(itemDetails.selectionRect)
                }
            })
            btnItemVersetFav.setOnClickListener { _ ->
                verset?.let {
                    val kind = if (it.isFavorite)
                        FavoriteAction.Remove(it.key)
                    else
                        FavoriteAction.Add(it.key)
                    action(kind)
                }
            }
            btnItemVersetInfo.setOnClickListener { _ ->
                verset?.let {
                    action(FavoriteAction.Info(it.key))
                }
            }
        }

    }

    fun bind(v: Read.Verset?, position: Int, key: Long) {
        verset = v
        with(itemDetails) {
            this.adapterPosition = position
            this.key = key
        }
        if (key == RecyclerView.NO_ID) {
            selectionTracker.deselect(key)
        }

        val layout = itemView.cast<MotionLayout>()
        with(layout) {
            textItemFavVerset.text = v?.content
            val isSelected = selectionTracker.isSelected(itemDetails.key)
            if (isSelected && !isVisualSelected) {
                layout.transitionToEnd()
            } else if (!isSelected && isVisualSelected) {
                layout.transitionToStart()
            }
        }
    }

    fun clear() {

    }
}

sealed class FavoriteAction(val key: VersetKey) {
    class Add(key: VersetKey) : FavoriteAction(key)
    class Remove(key: VersetKey) : FavoriteAction(key)
    class Info(key: VersetKey) : FavoriteAction(key)
}