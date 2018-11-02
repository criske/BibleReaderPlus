/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read

import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.presentation.util.arch.*
import com.crskdev.biblereaderplus.presentation.util.system.dpToPx
import com.crskdev.biblereaderplus.presentation.util.system.hideSoftKeyboard
import kotlinx.android.synthetic.main.fragment_contents.*


class ContentsFragment : Fragment() {

    companion object {
        const val SCROLL_SOURCE = 1
    }

    private lateinit var sharedViewModel: ReadViewModel

    private lateinit var contentsViewModel: ContentsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = viewModelFromProvider(parentFragment!!) {
            ReadViewModel()
        }
        contentsViewModel = viewModelFromProvider(this) {
            ContentsViewModel()
        }
    }

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
            adapter = ContentsAdapter(LayoutInflater.from(context)) {
                textInputLayoutContents.editText?.clearFocus()
                val readKey = it.getKey()
                sharedViewModel.scrollTo(SCROLL_SOURCE, readKey)
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
        sharedViewModel.scrollReadLiveData
            .observe(this, Observer {
                contentsViewModel.scrollTo(it.readKey)
            })
        contentsViewModel.scrollPositionLiveData.observe(this, Observer {
            recyclerContents.smoothScrollToPosition(it)
        })
        contentsViewModel.contentsLiveData.observe(this, Observer {
            recyclerContents.adapter?.cast<ContentsAdapter>()?.submit(it)
        })
    }

}


class ContentsViewModel : CoroutineScopedViewModel() {

    private val searchBookLiveData: MutableLiveData<String> = MutableLiveData<String>()
        .apply { value = "" }

    private val dataSourceLiveData: LiveData<List<ReadUI>> =
        searchBookLiveData.switchMap { bookTerm ->
            val original = MOCKED_BIBLE_DATA_SOURCE.filter { it !is ReadUI.VersetUI }
            val dataSource = if (bookTerm.isEmpty() || bookTerm.length < 3) {
                original
            } else {
                val candidates = original
                    .filter { it is ReadUI.BookUI && it.name.contains(bookTerm, true) }
                    .map { it.cast<ReadUI.BookUI>() }
                original.filter { read ->
                    if (read is ReadUI.ChapterUI) {
                        candidates.any { read.cast<ReadUI.ChapterUI>().bookId == it.id }
                    } else
                        candidates.contains(read)
                }
            }
            LiveDataCompanions.just(dataSource)
        }

    private val scrollReadLiveData: MutableLiveData<ReadKey> = MutableLiveData<ReadKey>().apply {
        value = ReadKey.INITIAL
    }

    private val scrollPositionAndContentsLiveData: LiveData<Pair<Int, List<ReadUI>>> =
        scrollReadLiveData
            .combineLatest(dataSourceLiveData)
            .map { pair ->
                //TODO go functional with this logic
                val key = pair.first
                var position = -1
                val (b, ch, v) = key
                var selected = false
                val source = pair.second
                    .mapIndexed { index, item ->
                        if (selected)
                            item
                        else {
                            val (ib, ich, iv) = item.getKey()
                            if ((ib == b && ich == ch)) {
                                position = index
                                selected = true
                                item.setHasScrollPosition(true)
                            } else {
                                item
                            }
                        }

                    }
                    .toList()
                position to source
            }

    val scrollPositionLiveData: LiveData<Int> =
        scrollPositionAndContentsLiveData.map { it.first }.filter { it != -1 }
    val contentsLiveData: LiveData<List<ReadUI>> =
        scrollPositionAndContentsLiveData.map { it.second }

    fun scrollTo(readKey: ReadKey) {
        scrollReadLiveData.value = readKey
    }

    fun search(book: String) {
        //TODO condition here
        searchBookLiveData.value = book
    }

}

