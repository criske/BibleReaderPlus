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
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.viewModelFromProvider
import kotlinx.android.synthetic.main.fragment_pages.*

class PagesFragment : Fragment() {

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
        recyclerPages.apply {
            adapter = PagesAdapter(LayoutInflater.from(context)) {

            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        layoutManager?.cast<LinearLayoutManager>()
                            ?.findFirstCompletelyVisibleItemPosition()
                            ?.let { position ->
                                adapter?.cast<PagesAdapter>()?.getItemAt(position)?.let {
                                    sharedViewModel.scrollTo(it.getKey())
                                }
                            }
                    }
                }
            })
        }
        sharedViewModel.scrollReadLiveData.observe(this, Observer {
            pagesViewModel.scrollTo(it)
        })
        pagesViewModel.scrollPositionLiveData.observe(this, Observer {
            recyclerPages.layoutManager?.cast<LinearLayoutManager>()?.scrollToPositionWithOffset(it, 0)
        })
        pagesViewModel.pagesLiveData.observe(this, Observer {
            recyclerPages.adapter?.cast<PagesAdapter>()?.submit(it)
        })
    }

}

class PagesViewModel : CoroutineScopedViewModel() {

    val scrollPositionLiveData: LiveData<Int> = MutableLiveData<Int>()

    val pagesLiveData: LiveData<List<ReadUI>> = MutableLiveData<List<ReadUI>>().apply {
        value = MOCKED_BIBLE_DATA_SOURCE
    }

    fun scrollTo(readKey: ReadKey) {
        val pages = pagesLiveData.value
        pages?.indexOfFirst { it.getKey() == readKey }
            ?.takeIf { it != -1 }
            ?.let { scrollPositionLiveData.cast<MutableLiveData<Int>>().value = it }
    }

}


class PagesAdapter(
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
        ReadAdapterHelper.onCreateViewHolder(inflater, parent, viewType, action)

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ReadVH<*>, position: Int) =
        ReadAdapterHelper.onBindViewHolder(holder, items[position])

    fun getItemAt(position: Int): ReadUI? = items.getOrNull(position)

}



