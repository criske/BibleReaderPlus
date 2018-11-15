package com.crskdev.biblereaderplus.domain.interactors.read

import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Created by Cristian Pela on 14.11.2018.
 */
interface ContentInteractor {

    suspend fun request(query: String? = null): Response

    sealed class Response {
        class OK(val result: List<Read.Content>) : Response()
        sealed class Error : Response() {
            object InvalidLength : Error()
        }
    }
}

class ContentInteractorImpl(
    private val dispatchers: GatewayDispatchers,
    private val repository: DocumentRepository) : ContentInteractor {

    override suspend fun request(query: String?): ContentInteractor.Response = coroutineScope {
        val sanitizedQuery = query?.trim()
        if (sanitizedQuery.isNullOrEmpty()) {
            withContext(dispatchers.DEFAULT) {
                ContentInteractor.Response.OK(repository.contents())
            }
        } else {
            if (sanitizedQuery.length >= 3) {
                withContext(dispatchers.DEFAULT) {
                    ContentInteractor.Response.OK(repository.filter(sanitizedQuery))
                }
            } else {
                ContentInteractor.Response.Error.InvalidLength
            }
        }
    }


}
