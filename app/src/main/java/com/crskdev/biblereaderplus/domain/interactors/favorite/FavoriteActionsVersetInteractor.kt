package com.crskdev.biblereaderplus.domain.interactors.favorite

import com.crskdev.biblereaderplus.common.util.launchIgnoreThrow
import com.crskdev.biblereaderplus.common.util.sendAndClose
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Created by Cristian Pela on 13.11.2018.
 */
interface FavoriteActionsVersetInteractor {

    suspend fun request(versetKey: VersetKey, addToFavorites: Boolean, response: (Response) -> Unit)

    sealed class Response {
        object Wait : Response()
        object OK : Response()
        class Error(@Suppress("unused") val err: Throwable) : Response()
    }

}

class FavoriteActionsVersetInteractorImpl @Inject constructor(
    private val dispatchers: GatewayDispatchers,
    private val repository: DocumentRepository) : FavoriteActionsVersetInteractor {

    @ObsoleteCoroutinesApi
    override suspend fun request(versetKey: VersetKey, addToFavorites: Boolean, response: (FavoriteActionsVersetInteractor.Response) -> Unit) =
        coroutineScope {
            val sendChannel = actor<FavoriteActionsVersetInteractor.Response> {
                for (r in channel) {
                    response(r)
                }
            }
            sendChannel.send(FavoriteActionsVersetInteractor.Response.Wait)
            val errHandler: suspend (CoroutineContext, Throwable) -> Unit = { _, throwable ->
                sendChannel.sendAndClose(FavoriteActionsVersetInteractor.Response.Error(throwable))
            }
            launchIgnoreThrow(dispatchers.IO, errHandler) {
                repository.favorite(versetKey, addToFavorites)
                sendChannel.sendAndClose(FavoriteActionsVersetInteractor.Response.OK)
            }
            Unit
        }

}


