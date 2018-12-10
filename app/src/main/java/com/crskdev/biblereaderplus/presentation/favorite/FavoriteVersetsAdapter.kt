/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.presentation.util.system.dpToPx
import com.crskdev.biblereaderplus.presentation.util.system.getColorCompat
import com.crskdev.biblereaderplus.presentation.util.view.BindableViewHolder
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
        getItem(position)?.let { holder.bind(it) } ?: holder.clear()
    }

    override fun onViewRecycled(holder: FavoriteVersetVH) {
        holder.clear()
    }
}


class FavoriteVersetVH(view: View,
                       private val selectionTracker: SelectionTracker<String>,
                       private val action: (FavoriteAction) -> Unit) :
    BindableViewHolder<Read.Verset>(view) {

    companion object {
        private const val TRANSITION_DETAIL_NAME_SUFFIX = "verset_transition_detail"
    }

    internal var itemDetails: FavoriteVersetItemDetails =
        FavoriteVersetItemDetails(RecyclerView.NO_POSITION, null)

    init {
        with(itemView) {
            textItemFavVerset.setOnLongClickListener {
                model?.let {
                    action(
                        FavoriteAction.Info(
                            it.key,
                            it.content,
                            textItemFavVerset to "$TRANSITION_DETAIL_NAME_SUFFIX$adapterPosition"
                        )
                    )
                }
                true
            }
            btnItemVersetFav.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    model?.let {
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
                    model?.let {
                        action(
                            FavoriteAction.Info(
                                it.key,
                                it.content,
                                textItemFavVerset to "$TRANSITION_DETAIL_NAME_SUFFIX$adapterPosition"
                            )
                        )
                    }
                }
                false
            }
        }
    }

    override fun onBind(model: Read.Verset) {
        with(itemDetails) {
            key = model.key.toString()
            adapterPosition = this@FavoriteVersetVH.adapterPosition
        }
        with(itemView) {
            textItemFavVerset.text = model.content
            ViewCompat.setTransitionName(textItemFavVerset, "detailTransition$adapterPosition")
            val isSelected = selectionTracker.isSelected(itemDetails.key).apply {
                btnItemVersetFav.isEnabled = this
                btnItemVersetInfo.isEnabled = this
            }
            btnItemVersetFav.setColorFilter(
                context.getColorCompat(if (model.isFavorite) R.color.likeColor else R.color.primaryDarkColor)
            )
            isActivated = isSelected
            swipeTextItemFavVerset(isSelected)
        }
    }

    private fun swipeTextItemFavVerset(selected: Boolean) {
        val offset = 40.dpToPx(itemView.resources).toFloat()
        val existentTranslationX = itemView.textItemFavVerset.translationX
        with(itemView.textItemFavVerset) {
            if (selected && existentTranslationX == 0f) {
                // if (isShown) {
                ObjectAnimator
                    .ofFloat(this, "translationX", 0f, -offset)
                    .setDuration(300)
                    .start()
//                } else {
//                    translationX = -offset
//                }
            } else
                translationX = 0f

        }


    }

    fun clear() {
        itemView.textItemFavVerset.text = null
    }
}

sealed class FavoriteAction(val key: VersetKey) {
    class Add(key: VersetKey) : FavoriteAction(key)
    class Remove(key: VersetKey) : FavoriteAction(key)
    class Info(key: VersetKey, val content: CharSequence, val transitionInfo: Pair<View, String>) :
        FavoriteAction(key)
}
