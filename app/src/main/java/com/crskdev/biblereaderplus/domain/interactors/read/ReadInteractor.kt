/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.read

import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Created by Cristian Pela on 08.11.2018.
 */
class ReadInteractor @Inject constructor(

    private val documentRepository: DocumentRepository) {


    suspend fun request(request: Request) = coroutineScope {

    }


    class Response

    class Request(val responseChannel: Channel<Response>)
}