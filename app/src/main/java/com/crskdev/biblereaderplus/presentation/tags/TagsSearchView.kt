/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.tags

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.domain.entity.Tag
import kotlinx.android.synthetic.main.tag_search_view_layout.view.*

/**
 * Created by Cristian Pela on 30.11.2018.
 */
class TagsSearchView : ConstraintLayout {

    private var listener: (Action) -> Unit = { _ -> }

    private var suggestionsAdapter: TagsAdapter

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    //@formatter:off
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.tag_search_view_layout, this, true)
        suggestionsAdapter = TagsAdapter(inflater, TagBehaviour(TagBehaviour.SelectPolicy.SELECT_ON_TAP)) { t, a ->
            when(a){
                TagSelectAction.CLOSE -> {}
                TagSelectAction.CONTEXT_MENU_RENAME ->  listener(Action.Rename(t))
                TagSelectAction.CONTEXT_MENU_REMOVE -> TODO()
                TagSelectAction.CONTEXT_MENU_CHANGE_COLOR -> TODO()
                TagSelectAction.SELECT -> listener(Action.Select(t))
            }
        }
        with(recyclerTagSearch) {
            adapter = suggestionsAdapter
            layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        }
        editTagSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btnTagSearchClear.isVisible = s.isNotBlank()
                ensurePaddingEnd()
                listener(Action.Query(s.toString().trim()))
            }
        })
        btnTagSearchClear.setOnClickListener {
            editTagSearch.setText("")
        }
        btnTagSearchAdd.setOnClickListener {
            listener(Action.Create(editTagSearch.text.toString()))
        }
    }
    //@formatter:on

    fun submitSuggestions(tags: List<Tag>) {
        btnTagSearchAdd.isVisible = tags.isEmpty() &&
                editTagSearch.let { it.text.isNotBlank() && it.length() > 2 }
        ensurePaddingEnd()
        suggestionsAdapter.submitList(tags)
    }

    fun setQuery(query: CharSequence) {
        editTagSearch.setText(query)
    }

    private fun ensurePaddingEnd() {
        //TODO: should really calculate or leave it hardcoded from xml?
//        val clearSpace = btnTagSearchClear.takeIf { it.isVisible }
//            ?.let { it.width + it.marginEnd + it.marginStart } ?: 0
//        val addSpace = btnTagSearchAdd.takeIf { it.isVisible }
//            ?.let { it.width + it.marginEnd + it.marginStart } ?: 0
//        editTagSearch.setPadding(left, top, clearSpace + addSpace, bottom)
    }

    fun onSearchListener(listener: (Action) -> Unit) {
        this.listener = listener
    }

    sealed class Action {
        class Select(val tag: Tag) : Action()
        class Query(val query: String) : Action()
        class Create(val tagName: String) : Action()
        class Rename(val tag: Tag) : Action()
    }
}