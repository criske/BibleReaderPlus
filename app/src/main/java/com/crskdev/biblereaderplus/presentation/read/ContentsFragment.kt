/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read

import android.graphics.Rect
import android.os.Bundle
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
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.viewModelFromProvider
import com.crskdev.biblereaderplus.presentation.util.system.dpToPx
import kotlinx.android.synthetic.main.fragment_contents.*


class ContentsFragment : Fragment() {

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
        recyclerContents.apply {
            adapter = ContentsAdapter(LayoutInflater.from(context)) {
                sharedViewModel.scrollTo(it.getKey())
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
            contentsViewModel.scrollTo(it)
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

    val scrollPositionLiveData: LiveData<Int> = MutableLiveData<Int>()

    val contentsLiveData: LiveData<List<ReadUI>> = MutableLiveData<List<ReadUI>>().apply {
        value = MOCKED_BIBLE_DATA_SOURCE.asSequence().filter { it !is ReadUI.VersetUI }.toList()
    }

    fun scrollTo(readKey: ReadKey) {
        val pages = contentsLiveData.value
        pages?.indexOfFirst { it.getKey() == readKey }
            ?.takeIf { it != -1 }
            ?.let { scrollPositionLiveData.cast<MutableLiveData<Int>>().value = it }
    }

}

class ContentsAdapter(
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

}
