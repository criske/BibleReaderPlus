/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.biblereaderplus.domain.entity.Read


/**
 * Created by Cristian Pela on 25.11.2018.
 */

class FavoriteVersetKeyProvider(var list: List<Read.Verset> = emptyList()) :
    ItemKeyProvider<String>(SCOPE_MAPPED) {

    override fun getKey(position: Int): String? = list.getOrNull(position)?.key?.toString()

    override fun getPosition(key: String): Int = list.indexOfFirst { it.key.toString() == key }

}


class FavoriteVersetLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {

    override fun getItemDetails(e: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if (view != null) {
            val holder = recyclerView.getChildViewHolder(view)
            if (holder is FavoriteVersetVH) {
                return holder.itemDetails
            }
        }
        return null
    }
}

class FavoriteVersetItemDetails(var adapterPosition: Int, var key: String?) :
    ItemDetailsLookup.ItemDetails<String>() {

    override fun getSelectionKey(): String? = key

    override fun getPosition(): Int = adapterPosition

    override fun inSelectionHotspot(e: MotionEvent): Boolean {
        //TODO better hotspot
        return e.x < 200f
    }
}
