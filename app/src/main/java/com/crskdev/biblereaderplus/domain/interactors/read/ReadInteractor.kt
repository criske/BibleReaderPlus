/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.crskdev.biblereaderplus.domain.interactors.read

import androidx.paging.PagedList
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Cristian Pela on 08.11.2018.
 */
class ReadInteractor @Inject constructor(
    private val dispatchers: GatewayDispatchers,
    private val documentRepository: DocumentRepository) {


    suspend fun request(request: Request) = coroutineScope {
        val sendChannel = actor<PagedList<Read>> {
            channel.consumeEach {
                request.responseChannel.send(Response.Paged(it))
            }
        }
        launch {
            documentRepository.read(sendChannel)
        }

    }


    sealed class Response {
        class Paged(val list: PagedList<Read>) : Response()
    }

    class Request(val responseChannel: SendChannel<Response>)
}