/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.crskdev.biblereaderplus.domain.interactors.read

import androidx.paging.PagedList
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Created by Cristian Pela on 08.11.2018.
 */
class ReadInteractor @Inject constructor(
    private val documentRepository: DocumentRepository) {
    
    suspend fun request(request: Request) = coroutineScope {

    }


    sealed class Response {
        class Paged(val list: PagedList<Read>) : Response()
    }

    class Request(val responseChannel: SendChannel<Response>)
}