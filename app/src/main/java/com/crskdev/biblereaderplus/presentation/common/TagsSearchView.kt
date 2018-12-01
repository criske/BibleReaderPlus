/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.common

import android.content.Context
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.presentation.favorite.TagBehaviour
import com.crskdev.biblereaderplus.presentation.favorite.TagsAdapter
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.tag_search_view_layout.view.*

/**
 * Created by Cristian Pela on 30.11.2018.
 */
class TagsSearchView : LinearLayout {

    private var listener: (Action) -> Unit = { _ -> }

    private var suggestionsAdapter: TagsAdapter

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.tag_search_view_layout, this, true)
        with(recyclerTagSearch) {
            suggestionsAdapter = TagsAdapter(inflater, TagBehaviour(isSelectable = true)) { t, _ ->
                listener(Action.Select(t))
            }
            adapter = suggestionsAdapter
            layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        }
        listener(Action.Query(""))
        editTagSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                listener(Action.Query(s.toString()))
            }
        })
    }

    fun submitSuggestions(tags: List<Tag>) {
        suggestionsAdapter.submitList(tags)
    }

    fun setQuery(query: CharSequence) {
        editTagSearch.setText(query)
    }

    fun onSearchListener(listener: (Action) -> Unit) {
        this.listener = listener
    }


    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }

    sealed class Action {
        class Select(val tag: Tag) : Action()
        class Query(val query: String) : Action()
    }

    @Parcelize
    private class State(val query: String) : Parcelable
}