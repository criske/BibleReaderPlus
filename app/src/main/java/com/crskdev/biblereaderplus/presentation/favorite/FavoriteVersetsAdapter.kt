/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.paging.PagedListAdapter
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
class FavoriteVersetsAdapter(private val inflater: LayoutInflater, private val action: (FavoriteAction) -> Unit) :
    PagedListAdapter<Read.Verset, FavoriteVersetVH>(object : DiffUtil.ItemCallback<Read.Verset>() {
        override fun areItemsTheSame(oldItem: Read.Verset, newItem: Read.Verset): Boolean =
            oldItem.key == newItem.key

        override fun areContentsTheSame(oldItem: Read.Verset, newItem: Read.Verset): Boolean =
            oldItem == newItem
    }) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteVersetVH =
        FavoriteVersetVH(inflater.inflate(R.layout.item_verset, parent, false))


    override fun onBindViewHolder(holder: FavoriteVersetVH, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: FavoriteVersetVH) {
        holder.clear()
    }
}


class FavoriteVersetVH(view: View) : RecyclerView.ViewHolder(view) {

    private var verset: Read.Verset? = null

    fun bind(v: Read.Verset?) {
        verset = v
        with(itemView) {
            textItemFavVerset.text = v?.content
        }
    }

    fun clear() {
        itemView.cast<MotionLayout>().transitionToStart()
    }
}

sealed class FavoriteAction(val key: VersetKey) {
    class Add(key: VersetKey) : FavoriteAction(key)
    class Remove(key: VersetKey) : FavoriteAction(key)
    class Info(key: VersetKey) : FavoriteAction(key)
}
