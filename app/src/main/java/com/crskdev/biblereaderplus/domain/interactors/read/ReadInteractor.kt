/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
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

    suspend fun request(decorator: (Read) -> Read, response: (PagedList<Read>) -> Unit)
}

/**
 * Created by Cristian Pela on 08.11.2018.
 */
class ReadInteractorImpl @Inject constructor(
    private val dispatchers: GatewayDispatchers,
    private val documentRepository: DocumentRepository) : ReadInteractor {

    override suspend fun request(decorator: (Read) -> Read, response: (PagedList<Read>) -> Unit) =
        coroutineScope {
            launch {
                documentRepository.read()
                    .mapByPage { list -> list.map { decorator.invoke(it) } }
                    .onPagingWithDefaultPagedListBuilder(dispatchers.DEFAULT, response)
            }
            Unit
        }

}