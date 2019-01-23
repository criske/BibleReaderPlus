/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.crskdev.biblereaderplus.domain.interactors.read

import androidx.paging.PagedList
import com.crskdev.biblereaderplus.common.util.pagedlist.onPagingWithDefaultPagedListBuilder
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ReadInteractor {

    suspend fun <R> request(decorator: (Read) -> R, response: (PagedList<R>) -> Unit)
}

/**
 * Created by Cristian Pela on 08.11.2018.
 */
class ReadInteractorImpl @Inject constructor(
    private val dispatchers: GatewayDispatchers,
    private val documentRepository: DocumentRepository) : ReadInteractor {

    override suspend fun <R> request(decorator: (Read) -> R, response: (PagedList<R>) -> Unit) =
        coroutineScope {
            launch(dispatchers.DEFAULT) {
                documentRepository.read()
                    .mapByPage { list -> list.map { decorator.invoke(it) } }
                    .onPagingWithDefaultPagedListBuilder<Int, R>(dispatchers.DEFAULT, response)
            }
            Unit
        }

}