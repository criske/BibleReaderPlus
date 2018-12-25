/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.interactors.read.ReadInteractor
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.SingleLiveEvent
import com.crskdev.biblereaderplus.presentation.util.arch.filter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_pages.*
import kotlinx.coroutines.launch
import javax.inject.Inject


class PagesFragment : DaggerFragment() {

    companion object {
        const val SCROLL_SOURCE = 2
    }

    @Inject
    lateinit var sharedViewModel: ReadViewModel

    @Inject
    lateinit var pagesViewModel: PagesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val snapTopSmoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int = LinearSmoothScroller.SNAP_TO_START
        }
        recyclerPages.apply {
            adapter = PagesAdapter(LayoutInflater.from(context)) {

            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                        layoutManager?.cast<LinearLayoutManager>()
                            ?.findFirstCompletelyVisibleItemPosition()
                            ?.let { position ->
                                adapter?.cast<PagesAdapter>()?.getKeyAt(position)?.let {
                                    sharedViewModel.scrollTo(SCROLL_SOURCE, it)
                                }
                            }
                    }
                }
            })
        }
        sharedViewModel.scrollReadLiveData
            .filter { it?.source != SCROLL_SOURCE }//not interested of own scroll events
            .observe(this, Observer {
                pagesViewModel.scrollTo(it.readKey)
            })
        pagesViewModel.scrollPositionLiveData.observe(this, Observer {
            recyclerPages.layoutManager?.cast<LinearLayoutManager>()
                // ?.smoothScrollToPosition(snapTopSmoothScroller, it)
                ?.scrollToPositionWithOffset(it, 0)
        })
        pagesViewModel.pagesLiveData.observe(this, Observer {
            recyclerPages.adapter?.cast<PagesAdapter>()?.submitList(it)
        })
    }

}

class PagesViewModel(private val readInteractor: ReadInteractor) : CoroutineScopedViewModel() {

    val scrollPositionLiveData: LiveData<Int> = SingleLiveEvent<Int>()

    val pagesLiveData: LiveData<PagedList<ReadUI>> = MutableLiveData<PagedList<ReadUI>>()

    init {
        launch {
            readInteractor.request(
                decorator = {
                    when (it) {
                        is Read.Content.Book -> ReadUI.BookUI(
                            it.id,
                            it.name
                        )
                        is Read.Content.Chapter -> ReadUI.ChapterUI(
                            it.id,
                            it.key.bookId,
                            "Chapter ${it.number}"
                        )
                        is Read.Verset -> ReadUI.VersetUI(
                            it.id,
                            it.key.bookId,
                            it.key.chapterId,
                            it.number,
                            "${it.number}.${it.content}"
                        )
                    }
                }
            ) {
                pagesLiveData.cast<MutableLiveData<PagedList<ReadUI>>>().postValue(it)
            }
        }
    }

    fun scrollTo(readKey: ReadKey) {
        scrollPositionLiveData.cast<MutableLiveData<Int>>().value = readKey()
    }

}



