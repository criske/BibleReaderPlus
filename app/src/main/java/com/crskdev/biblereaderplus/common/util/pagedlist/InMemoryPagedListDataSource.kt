/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.common.util.pagedlist

import androidx.paging.PositionalDataSource
import com.crskdev.biblereaderplus.common.util.println

/**
 * Created by Cristian Pela on 12.11.2018.
 */
class InMemoryPagedListDataSource<T>(private val listProvider: () -> List<T>) : PositionalDataSource<T>() {

    companion object {
        fun <T> justWith(list: List<T>): InMemoryPagedListDataSource<T> = InMemoryPagedListDataSource { list }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<T>) {
        val list = listProvider()
        val endPosition =
            (params.startPosition + params.loadSize).coerceAtMost(list.size)
        val subList = list.subList(params.startPosition, endPosition)
            .apply { ("Range ${params.startPosition} $endPosition  $this").println() }
        callback.onResult(subList)
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<T>) {
        val list = listProvider()
        val totalCount = list.size
        val firstLoadPosition =
            PositionalDataSource.computeInitialLoadPosition(
                params,
                totalCount
            )
        val firstLoadSize =
            PositionalDataSource.computeInitialLoadSize(
                params,
                firstLoadPosition,
                totalCount
            )
        val lastLoadPosition = firstLoadPosition + firstLoadSize
        val sublist = list.subList(firstLoadPosition, lastLoadPosition).apply {
            ("Initial $firstLoadPosition load $firstLoadSize $totalCount ${params.requestedStartPosition} ${params.requestedLoadSize} $this").println()
        }


        callback.onResult(
            sublist,
            firstLoadPosition,
            totalCount
        )
    }
}