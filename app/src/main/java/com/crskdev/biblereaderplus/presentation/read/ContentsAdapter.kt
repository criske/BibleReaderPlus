/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ContentsAdapter(
    private val inflater: LayoutInflater,
    private val action: (ReadUI) -> Unit
) : RecyclerView.Adapter<ReadVH<*>>() {

    private val items = mutableListOf<ReadUI>()

    fun submit(newItems: List<ReadUI>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
        ReadAdapterHelper.getItemViewType(items[position])

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadVH<*> =
        ReadAdapterHelper.onCreateViewHolder(
            inflater,
            parent,
            viewType,
            action
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ReadVH<*>, position: Int) =
        ReadAdapterHelper.onBindViewHolder(holder, items[position])

}