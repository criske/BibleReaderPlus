/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import android.graphics.Rect
import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.biblereaderplus.domain.entity.Read
import kotlinx.android.synthetic.main.item_verset.view.*


/**
 * Created by Cristian Pela on 25.11.2018.
 */

class FavoriteVersetKeyProvider(var list: List<Read.Verset> = emptyList()) :
    ItemKeyProvider<String>(SCOPE_MAPPED) {

    override fun getKey(position: Int): String? = list.getOrNull(position)?.key?.toString()

    override fun getPosition(key: String): Int = list.indexOfFirst { it.key.toString() == key }

}


class FavoriteVersetLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {

    private val rect = Rect()

    override fun getItemDetails(e: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        //  if (e.action == MotionEvent.ACTION_UP) {
        if (view != null) {
            val holder = recyclerView.getChildViewHolder(view)
            if (holder is FavoriteVersetVH) {
                holder.itemView.textItemFavVerset.getGlobalVisibleRect(rect)
                println("Item details: $e")
                println("Item details rect: $rect")
                //if (rect.contains(e.rawX.toInt(), e.rawY.toInt())) {
                    return holder.itemDetails
                //}
            }
        }
        // }
        return null
    }
}

class VersetItemDetails(var adapterPosition: Int, var key: String?, var selectionRect: Rect) :
    ItemDetailsLookup.ItemDetails<String>() {

    override fun getSelectionKey(): String? = key

    override fun getPosition(): Int = adapterPosition

    override fun inSelectionHotspot(e: MotionEvent): Boolean = true
}