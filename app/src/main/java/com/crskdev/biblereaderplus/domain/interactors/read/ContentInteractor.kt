/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.read

import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * Created by Cristian Pela on 14.11.2018.
 */
interface ContentInteractor {

    suspend fun request(query: String? = null, response: (Response) -> Unit)

    sealed class Response(val result: List<Read.Content>?, val error: ContentInteractor.Error?) {
        class OK(result: List<Read.Content>) : Response(result, null)
        class Error(error: ContentInteractor.Error) : Response(null, error)

    }

    sealed class Error : Throwable() {
        object InvalidLength : Error()
        class Unknown(val throwable: Throwable) : Error()
    }
}

@ObsoleteCoroutinesApi
class ContentInteractorImpl(
    private val dispatchers: GatewayDispatchers,
    private val repository: DocumentRepository) : ContentInteractor {

    override suspend fun request(query: String?, response: (ContentInteractor.Response) -> Unit) =
        supervisorScope {
            val sanitizedQuery = query?.trim()
            val sendChannel = actor<ContentInteractor.Response> {
                for (r in channel) {
                    response(r)
                }
            }
            val errorHandler = CoroutineExceptionHandler { _, t ->
                if (t is ContentInteractor.Error) {
                    sendChannel.offer(ContentInteractor.Response.Error(t))
                } else {
                    sendChannel.offer(
                        ContentInteractor.Response.Error(
                            ContentInteractor.Error.Unknown(t)
                        )
                    )
                }
            }
            if (sanitizedQuery.isNullOrEmpty()) {
                launch(errorHandler + dispatchers.DEFAULT) {
                    sendChannel.send(ContentInteractor.Response.OK(repository.contents()))
                }
            } else {
                if (sanitizedQuery.length >= 3) {
                    launch(errorHandler + dispatchers.DEFAULT) {
                        sendChannel.send(
                            ContentInteractor.Response.OK(repository.filterContents(sanitizedQuery))
                        )
                    }
                } else {
                    throw ContentInteractor.Error.InvalidLength
                }
            }
            Unit
        }

}
