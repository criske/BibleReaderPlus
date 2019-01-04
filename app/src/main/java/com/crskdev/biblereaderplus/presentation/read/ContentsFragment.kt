/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.presentation.read

import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.presentation.util.system.dpToPx
import com.crskdev.biblereaderplus.presentation.util.system.hideSoftKeyboard
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_contents.*
import javax.inject.Inject


class ContentsFragment : DaggerFragment() {

    companion object {
        const val SCROLL_SOURCE = 1
    }

    @Inject
    lateinit var readViewModel: ReadViewModel

    @Inject
    lateinit var contentsViewModel: ContentsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textInputLayoutContents?.editText?.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    contentsViewModel.search(s.toString())
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                    Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) =
                    Unit

            })
            setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) activity?.hideSoftKeyboard(v)
            }
        }
        recyclerContents.apply {
            adapter = ContentsAdapter(LayoutInflater.from(context)) { action, item ->
                textInputLayoutContents.editText?.clearFocus()
                when (action) {
                    ContentsAdapter.Action.SELECT -> readViewModel.scrollTo(
                        SCROLL_SOURCE,
                        item.getKey()
                    )
                    ContentsAdapter.Action.EXPAND -> contentsViewModel.expand(
                        item.getKey(),
                        item.isExpanded
                    )
                }
            }
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val level = view.tag?.cast<Int>() ?: 0
                    outRect.set(level * 16.dpToPx(resources), 0, 0, 0)
                }
            })
        }
        readViewModel.scrollReadLiveData.observe(this, Observer {
            contentsViewModel.scrollTo(it.readKey)
        })
        contentsViewModel.scrollPositionLiveData.observe(this, Observer {
            recyclerContents.smoothScrollToPosition(it)
        })
        contentsViewModel.contentsLiveData.observe(this, Observer {
            recyclerContents.adapter?.cast<ContentsAdapter>()?.submitList(it)
        })
    }

}


