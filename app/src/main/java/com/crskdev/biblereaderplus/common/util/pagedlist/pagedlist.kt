/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.common.util.pagedlist

import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PagedList.Config.MAX_SIZE_UNBOUNDED
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor

/**
 * Created by Cristian Pela on 12.11.2018.
 */

inline fun <Key, Value> dataSourceFactory(crossinline block: () -> DataSource<Key, Value>): DataSource.Factory<Key, Value> =
    object : DataSource.Factory<Key, Value>() {
        override fun create(): DataSource<Key, Value> = block()
    }

fun <Key, Value> DataSource.Factory<Key, Value>.setupPagedListBuilder(block: PagedListBuilderDSL<Value>.() -> Unit):
        ReadyPagedListBuilder<Key, Value> =
    ReadyPagedListBuilder.createWith(this, PagedListBuilderDSL<Value>().apply(block))

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
suspend fun <Key, Value> ReadyPagedListBuilder<Key, Value>.onPaging(consumer: (PagedList<Value>) -> Unit) =
    coroutineScope {

        val sendChannel = actor<PagedList<Value>> {
            for (page in channel) {
                consumer(page)
            }
        }
        var lastPage: PagedList<Value>? = null
        val invalidatedCallback = object : DataSource.InvalidatedCallback {
            override fun onInvalidated() {
                val dis = this
                runBlocking(coroutineContext + fetchDispatcher) {
                    val lastKey = lastPage?.lastKey as? Key
                    do {
                        lastPage?.dataSource?.removeInvalidatedCallback(dis)
                        lastPage = recreate(lastKey).build().apply {
                            dataSource.addInvalidatedCallback(dis)
                        }
                    } while (lastPage?.isDetached == true)
                    lastPage?.let { sendChannel.send(it) }
                    Unit
                }
            }
        }

        sendChannel.send(build().apply {
            lastPage = this
            lastPage?.dataSource?.addInvalidatedCallback(invalidatedCallback)
        })
    }

internal suspend fun <Key, Value> ReadyPagedListBuilder<Key, Value>.build(): PagedList<Value> =
    coroutineScope {
        val pagedList = builder
            .setNotifyExecutor {
                runBlocking {
                    withContext(coroutineContext + notifyDispatcher) {
                        it.run()
                    }

                }
            }.setFetchExecutor {
                runBlocking {
                    withContext(coroutineContext + fetchDispatcher) {
                        it.run()
                    }
                }
            }
            .setBoundaryCallback(boundaryCallback)
            .build()
        pagedList
    }


class ConfigDSL {
    var enablePlaceholders: Boolean = true
    var initialLoadSizeHint: Int = -1
    var pageSize = -1
    var prefetchDistance = -1
    var maxSize = MAX_SIZE_UNBOUNDED

    operator fun invoke(pageSize: Int = 1, block: ConfigDSL.() -> Unit = {}) {
        this.pageSize = pageSize
        this.block()
    }
}

class PagedListBuilderDSL<Value> {
    var configDSL: ConfigDSL = ConfigDSL()
    var boundaryCallback: PagedList.BoundaryCallback<Value>? = null
    var fetchDispatcher: CoroutineDispatcher = Dispatchers.Default
    var notifyDispatcher: CoroutineDispatcher = Dispatchers.Main
}


class ReadyPagedListBuilder<Key, Value> internal constructor(
    internal val builder: PagedList.Builder<Key, Value>,
    internal val config: PagedList.Config,
    internal val dataSourceFactory: DataSource.Factory<Key, Value>,
    internal val boundaryCallback: PagedList.BoundaryCallback<Value>?,
    internal val notifyDispatcher: CoroutineDispatcher,
    internal val fetchDispatcher: CoroutineDispatcher) {

    companion object {
        internal fun <Key, Value> createWith(factory: DataSource.Factory<Key, Value>,
                                             pagedListDSL: PagedListBuilderDSL<Value>): ReadyPagedListBuilder<Key, Value> {
            val dataSource = factory.create()
            val config = PagedList.Config.Builder()
                .setEnablePlaceholders(pagedListDSL.configDSL.enablePlaceholders)
                .setInitialLoadSizeHint(pagedListDSL.configDSL.initialLoadSizeHint)
                .setPageSize(pagedListDSL.configDSL.pageSize)
                .setMaxSize(pagedListDSL.configDSL.maxSize)
                .setPrefetchDistance(pagedListDSL.configDSL.prefetchDistance)
                .build()

            val builder = PagedList.Builder<Key, Value>(dataSource, config)

            return ReadyPagedListBuilder(
                builder,
                config,
                factory,
                pagedListDSL.boundaryCallback,
                pagedListDSL.notifyDispatcher,
                pagedListDSL.fetchDispatcher
            )

        }
    }


    internal fun recreate(initialKey: Key? = null): ReadyPagedListBuilder<Key, Value> {
        val dataSource = dataSourceFactory.create()
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(config.enablePlaceholders)
            .setInitialLoadSizeHint(config.initialLoadSizeHint)
            .setPageSize(config.pageSize)
            .setMaxSize(config.maxSize)
            .setPrefetchDistance(config.prefetchDistance)
            .build()
        val builder = PagedList.Builder<Key, Value>(dataSource, config)
            .setInitialKey(initialKey)
        return ReadyPagedListBuilder(
            builder,
            config,
            dataSourceFactory,
            boundaryCallback,
            notifyDispatcher,
            fetchDispatcher
        )
    }
}

        
