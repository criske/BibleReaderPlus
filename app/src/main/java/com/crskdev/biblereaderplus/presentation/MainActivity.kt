/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */
package com.crskdev.biblereaderplus.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.DataSource
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.crskdev.arch.couroutines.paging.dataSourceFactory
import com.crskdev.arch.couroutines.paging.onPaging
import com.crskdev.arch.couroutines.paging.setupPagedListBuilder
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.common.util.pagedlist.InMemoryPagedListDataSource
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main_pagedlist_test.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainActivity : DaggerAppCompatActivity(), CoroutineScope {


    private val job = Job()

    override val coroutineContext: CoroutineContext = job + Dispatchers.Main


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        dynamicallyLoadNavGraph(
//            R.id.container,
//            R.navigation.main_nav_graph,
//            R.id.setupFragment,
//            supportFragmentManager,
//            savedInstanceState
//        )
        setContentView(R.layout.activity_main_pagedlist_test)
        setupUI()
    }

    private fun setupUI() {
        with(recyclerPagedTest) {
            val testAdapter = TestAdapter()
            adapter = testAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            launch {
                dataSourceFactory {
                    (0..1000).fold(mutableListOf<String>()) { a, c ->
                        a.apply { add("Item$c") }
                    }.let {
                        InMemoryPagedListDataSource(it)
                    }
                }.setupPagedListBuilder {
                    config {
                        pageSize = 10
                    }
                }.onPaging {
                    button.tag = it.dataSource
                    testAdapter.cast<TestAdapter>().submitList(it)
                }
            }
            Unit
        }
        button.setOnClickListener {
            it.tag!!.cast<DataSource<Int, String>>().invalidate()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
    //  override fun onSupportNavigateUp() = findNavController(R.id.container).navigateUp()

    @SuppressLint("RestrictedApi")
    class TestAdapter : PagedListAdapter<String, VH>(
        AsyncDifferConfig.Builder<String>(object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }).build()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(
                LayoutInflater.from(parent.context).inflate(
                    android.R.layout.simple_list_item_1,
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.onBind(getItem(position) ?: "")
        }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {

        private val textView by lazy(LazyThreadSafetyMode.NONE) {
            itemView.findViewById<TextView>(android.R.id.text1)
        }

        fun onBind(item: String) {
            textView.text = item
        }

    }

}
