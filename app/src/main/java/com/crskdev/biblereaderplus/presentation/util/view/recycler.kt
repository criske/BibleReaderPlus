/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.view

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Cristian Pela on 01.11.2018.
 */
@Suppress("UNCHECKED_CAST")
abstract class BindableViewHolder<M>(v: View) : RecyclerView.ViewHolder(v) {

    @Suppress("MemberVisibilityCanBePrivate")
    protected var model: M? = null

    fun bind(model: Any) {
        this.model = model as M
        onBind(model)
    }

    open fun unbind() {}

    abstract fun onBind(model: M)
}

fun LinearLayoutManager.smoothScrollToPosition(
    smoothScroller: RecyclerView.SmoothScroller,
    position: Int
) {
    smoothScroller.targetPosition = position
    this.startSmoothScroll(smoothScroller)
}