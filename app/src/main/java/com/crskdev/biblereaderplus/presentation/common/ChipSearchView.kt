/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.common

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import com.crskdev.biblereaderplus.presentation.util.view.autocompleteTextView

/**
 * Created by Cristian Pela on 27.11.2018.
 */
class ChipSearchView : SearchView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        doInit()
    }

    private fun doInit() {
        suggestionsAdapter = DummyAdapter(context, MatrixCursor(arrayOf("_id")))
        val autocompleteTextView = autocompleteTextView()
        autocompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val text = autocompleteTextView.text.trim()
            val chip = parent.getItemAtPosition(position).toString()
            autocompleteTextView.append(" " + parent.getItemAtPosition(position).toString())
        }
        autocompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                println("Text After: $s")
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                println("Text before $s start:$start count:$count after:$after")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                println("Text Chaged $s start:$start count:$count before:$before")
            }

        })
    }

    private class DummyAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor) {

        private val view by lazy {
            View(context)
        }

        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View = view

        override fun bindView(view: View, context: Context, cursor: Cursor?) = Unit

    }

}