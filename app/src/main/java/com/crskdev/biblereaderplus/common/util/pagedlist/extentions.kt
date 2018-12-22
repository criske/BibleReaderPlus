/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.common.util.pagedlist

import androidx.paging.DataSource
import androidx.paging.PagedList
import com.crskdev.arch.coroutines.paging.onPaging
import com.crskdev.arch.coroutines.paging.setupPagedListBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi


/**
 * Created by Cristian Pela on 22.12.2018.
 */

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
suspend inline fun <K, V> DataSource.Factory<K, V>.onPagingWithDefaultPagedListBuilder(
    bgDispatcher: CoroutineDispatcher = Dispatchers.Main,
    crossinline block: (PagedList<V>) -> Unit) {
    setupPagedListBuilder {
        config(10)
        fetchDispatcher = bgDispatcher
    }.onPaging { pagedList, _ -> block(pagedList) }
}