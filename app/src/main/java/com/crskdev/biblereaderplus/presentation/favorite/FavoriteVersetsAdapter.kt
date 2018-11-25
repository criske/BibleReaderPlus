/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import android.animation.Animator
import android.animation.LayoutTransition
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.MotionEvent
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

    lateinit var selectionTracker: SelectionTracker<String>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteVersetVH =
        FavoriteVersetVH(
            inflater.inflate(R.layout.item_verset, parent, false),
            selectionTracker,
            action
        )


    override fun onBindViewHolder(holder: FavoriteVersetVH, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: FavoriteVersetVH) {
        holder.clear()
    }
}


class FavoriteVersetVH(view: View,
                       private val selectionTracker: SelectionTracker<String>,
                       private val action: (FavoriteAction) -> Unit) :
    RecyclerView.ViewHolder(view) {

    private var verset: Read.Verset? = null

    private var animator: Animator? = null

    internal var itemDetails: VersetItemDetails =
        VersetItemDetails(RecyclerView.NO_POSITION, null, Rect())

    init {
        with(itemView.cast<MotionLayout>()) {
            textItemFavVerset.getHitRect(itemDetails.selectionRect)
            btnItemVersetFav.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    verset?.let {
                        val kind = if (it.isFavorite)
                            FavoriteAction.Remove(it.key)
                        else
                            FavoriteAction.Add(it.key)
                        action(kind)
                    }
                }
                true
            }
            btnItemVersetInfo.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    verset?.let {
                        action(FavoriteAction.Info(it.key))
                    }
                }
                true
            }
        }
    }

    fun bind(v: Read.Verset?) {
        verset = v
        with(itemDetails) {
            this.adapterPosition = this@FavoriteVersetVH.adapterPosition
            this.key = v?.key.toString()
        }
        val layout = itemView.cast<MotionLayout>()
        with(layout) {
            val isSelected = selectionTracker.isSelected(itemDetails.key)
            layout.isActivated = isSelected
            textItemFavVerset.text = v?.content
            if (animator?.isRunning == true) {
                animator?.cancel()
            }
            animator = layout.layoutTransition?.getAnimator(LayoutTransition.CHANGING)
            if (isSelected) {
                layout.transitionToEnd()
            } else {
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
