/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import android.graphics.Rect
import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_verset.view.*


/**
 * Created by Cristian Pela on 25.11.2018.
 */
class FavoriteVersetLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {

    private val rect = Rect()

    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (e.action == MotionEvent.ACTION_UP) {
            if (view != null) {
                val holder = recyclerView.getChildViewHolder(view)
                if (holder is FavoriteVersetVH) {
                    holder.itemView.textItemFavVerset.getGlobalVisibleRect(rect)
                    println("Item details: $e")
                    println("Item details rect: $rect")
                    if (rect.contains(e.rawX.toInt(), e.rawY.toInt())) {
                        return holder.itemDetails
                    }
                }
            }
        }
        return null
    }
}

class VersetItemDetails(var adapterPosition: Int, var key: Long?, var selectionRect: Rect) :

    ItemDetailsLookup.ItemDetails<Long>() {

    override fun getSelectionKey(): Long? = key

    override fun getPosition(): Int = adapterPosition

    override fun inSelectionHotspot(e: MotionEvent): Boolean = false
}