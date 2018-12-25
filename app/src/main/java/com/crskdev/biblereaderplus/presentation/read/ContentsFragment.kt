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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.interactors.read.ContentInteractor
import com.crskdev.biblereaderplus.presentation.util.arch.*
import com.crskdev.biblereaderplus.presentation.util.system.dpToPx
import com.crskdev.biblereaderplus.presentation.util.system.hideSoftKeyboard
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_contents.*
import kotlinx.coroutines.launch
import javax.inject.Inject


class ContentsFragment : DaggerFragment() {

    companion object {
        const val SCROLL_SOURCE = 1
    }

    @Inject
    lateinit var sharedViewModel: ReadViewModel

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
        sharedViewModel.scrollReadLiveData.observe(this, Observer {
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


class ContentsViewModel(private val contentInteractor: ContentInteractor) :
    CoroutineScopedViewModel() {

    private val searchBookLiveData: MutableLiveData<String> = MutableLiveData<String>()
        .apply { value = "" }

    private val contentLiveData: LiveData<List<ReadUI.ContentUI>> =
        MutableLiveData<List<ReadUI.ContentUI>>()

    private val scrollReadLiveData: MutableLiveData<ReadKey> = MutableLiveData<ReadKey>().apply {
        value = ReadKey.INITIAL
    }

    init {
        launch {
            searchBookLiveData.toChannel {
                contentInteractor.request(it) { response ->
                    when (response) {
                        is ContentInteractor.Response.OK -> {
                            val contentsUI = response.result!!.map { read ->
                                when (read) {
                                    is Read.Content.Book -> ReadUI.BookUI(
                                        read.id,
                                        read.name,
                                        HasScrollPosition(false),
                                        IsBookmarked(false)
                                    )
                                    is Read.Content.Chapter -> ReadUI.ChapterUI(
                                        read.id,
                                        read.key.bookId,
                                        read.number.toString(),
                                        HasScrollPosition(false),
                                        IsBookmarked(false)
                                    )
                                }
                            }
                            contentLiveData
                                .cast<MutableLiveData<List<ReadUI.ContentUI>>>()
                                .postValue(contentsUI)
                        }
                        is ContentInteractor.Response.Error -> {
                            //no-op}
                        }
                    }
                }
            }
        }

    }

    private val positionAndSelectionMapping: (Pair<ReadKey, List<ReadUI.ContentUI>>) -> (ReadPosToListReadUI) =
        { pair ->
            val positionKey = pair.first
            var closestContentKey = ReadKey.INITIAL
            var position = closestContentKey()
            val source = pair.second.toMutableList()
            source.forEachIndexed { index, item ->
                closestContentKey = item.getKey()
                if (closestContentKey() <= positionKey()) {
                    position = index
                } else {
                    return@forEachIndexed
                }
            }
            source[position] = source[position].setHasScrollPosition(true) as ReadUI.ContentUI
            position to source
        }

    private val scrollPositionAndContentsLiveData: LiveData<ReadPosToListReadUI> =
        scrollReadLiveData
            .combineLatest(contentLiveData)
            .map(positionAndSelectionMapping)
            .distinctUntilChanged { prev, curr ->
                prev.first != curr.first
            }

    val scrollPositionLiveData: LiveData<Int> =
        scrollPositionAndContentsLiveData.map { it.first }.filter { it != -1 }
    val contentsLiveData: LiveData<List<ReadUI.ContentUI>> =
        scrollPositionAndContentsLiveData.map { it.second }


    fun scrollTo(readKey: ReadKey) {
        scrollReadLiveData.value = readKey
    }

    fun search(book: String) {
        //TODO condition here
        searchBookLiveData.value = book
    }

}

typealias ReadPosToListReadUI = Pair<Int, List<ReadUI.ContentUI>>


