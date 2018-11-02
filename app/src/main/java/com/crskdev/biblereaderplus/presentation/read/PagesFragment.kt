/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.SingleLiveEvent
import com.crskdev.biblereaderplus.presentation.util.arch.filter
import com.crskdev.biblereaderplus.presentation.util.arch.viewModelFromProvider
import kotlinx.android.synthetic.main.fragment_pages.*


class PagesFragment : Fragment() {

    companion object {
        const val SCROLL_SOURCE = 2
    }

    private lateinit var sharedViewModel: ReadViewModel

    private lateinit var pagesViewModel: PagesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = viewModelFromProvider(parentFragment!!) {
            ReadViewModel()
        }
        pagesViewModel = viewModelFromProvider(this) {
            PagesViewModel()
        }
    }

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
                                adapter?.cast<PagesAdapter>()?.getItemAt(position)?.let {
                                    sharedViewModel.scrollTo(SCROLL_SOURCE, it.getKey())
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
            recyclerPages.adapter?.cast<PagesAdapter>()?.submit(it)
        })
    }

}

class PagesViewModel : CoroutineScopedViewModel() {

    val scrollPositionLiveData: LiveData<Int> = SingleLiveEvent<Int>()

    val pagesLiveData: LiveData<List<ReadUI>> = MutableLiveData<List<ReadUI>>().apply {
        value = MOCKED_BIBLE_DATA_SOURCE
    }

    fun scrollTo(readKey: ReadKey) {
        val pages = pagesLiveData.value
        pages?.indexOfFirst { it.getKey() == readKey }
            ?.takeIf { it != -1 }
            ?.let {
                scrollPositionLiveData.cast<MutableLiveData<Int>>().value = it
            }
    }

}



