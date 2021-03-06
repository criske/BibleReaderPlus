/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
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
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.interactors.read.ReadInteractor
import com.crskdev.biblereaderplus.presentation.common.CharSequenceTransformerFactory
import com.crskdev.biblereaderplus.presentation.favorite.VersetTransitions
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.SingleLiveEvent
import com.crskdev.biblereaderplus.presentation.util.arch.filter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_pages.*
import kotlinx.android.synthetic.main.read_float_action_menu.*
import kotlinx.coroutines.launch
import javax.inject.Inject


class PagesFragment : DaggerFragment() {

    companion object {
        const val SCROLL_SOURCE = 2
    }

    @Inject
    lateinit var readViewModel: ReadViewModel

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
        fabReadActionMenuFav.setOnClickListener {
            readViewModel.open(ReadViewModel.Open.Favorites)
        }
        fabReadActionMenuSearch.setOnClickListener {
            readViewModel.open(ReadViewModel.Open.SearchRead)
        }
        recyclerPages.apply {
            adapter = PagesAdapter(LayoutInflater.from(context)) {
                if (it is VersetTransitions.NavInfoExtra) {
                    readViewModel.open(ReadViewModel.Open.Verset(it))
                }
            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                        layoutManager?.cast<LinearLayoutManager>()
                            ?.findFirstCompletelyVisibleItemPosition()
                            ?.let { position ->
                                adapter?.cast<PagesAdapter>()?.getKeyAt(position)?.let {
                                    readViewModel.scrollTo(SCROLL_SOURCE, it)
                                }
                            }
                    }
                }
            })
        }
        readViewModel.scrollReadLiveData
            .filter { it?.source != SCROLL_SOURCE }//not interested of own scroll events
            .observe(this, Observer {
                pagesViewModel.scrollTo(it.readKey)
            })
        pagesViewModel.scrollPositionLiveData.observe(this, Observer {
            recyclerPages.layoutManager?.cast<LinearLayoutManager>()
                ?.scrollToPositionWithOffset(it, 0)
        })
        pagesViewModel.pagesLiveData.observe(this, Observer {
            recyclerPages.adapter?.cast<PagesAdapter>()?.submitList(it)
        })
    }

}

class PagesViewModel(private val readInteractor: ReadInteractor,
                     private val charSequenceTransformerFactory: CharSequenceTransformerFactory)
    : CoroutineScopedViewModel() {

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
                            charSequenceTransformerFactory.transform(
                                CharSequenceTransformerFactory.Type.LEAD_FIRST_LINE,
                                "${it.number}.${it.content}"
                            )
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



