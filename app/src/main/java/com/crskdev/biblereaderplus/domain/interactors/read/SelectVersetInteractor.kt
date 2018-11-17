package com.crskdev.biblereaderplus.domain.interactors.read

import com.crskdev.biblereaderplus.common.util.launchIgnoreThrow
import com.crskdev.biblereaderplus.common.util.retry
import com.crskdev.biblereaderplus.common.util.sendAndClose
import com.crskdev.biblereaderplus.domain.entity.SelectedVerset
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlin.coroutines.CoroutineContext

/**
 * Created by Cristian Pela on 13.11.2018.
 */
@Suppress("unused")
interface SelectVersetInteractor {

    suspend fun request(versetKey: VersetKey, response: (Response) -> Unit)

    sealed class Response {
        class OK(val result: SelectedVerset) : Response()
        class Partial(val result: SelectedVerset, val throwable: Throwable) : Response()
        object Wait : Response()
        sealed class Error : Response() {
            class GenericErr(val throwable: Throwable) : Error()
            object NotFound : Error()
        }
    }

}

@ObsoleteCoroutinesApi
class SelectVersetInteractorImpl(
    private val dispatchers: GatewayDispatchers,
    private val repository: DocumentRepository
) : SelectVersetInteractor {

    override suspend fun request(versetKey: VersetKey, response: (SelectVersetInteractor.Response) -> Unit) =
        coroutineScope {

            val sender = actor<SelectVersetInteractor.Response> {
                for (r in channel) {
                    response(r)
                }
            }
            sender.send(SelectVersetInteractor.Response.Wait)

            val handler: suspend (CoroutineContext, Throwable) -> Unit = { _, err ->
                sender.sendAndClose(
                    when (err) {
                        is ErrorResponseAdapter -> err.errorResponse
                        else -> SelectVersetInteractor.Response.Error.GenericErr(err)
                    }
                )
            }

            launchIgnoreThrow(handler = handler) {
                //local
                val verset = withContext(coroutineContext + dispatchers.DEFAULT) {
                    repository.getVerset(versetKey)
                        ?: throw ErrorResponseAdapter(SelectVersetInteractor.Response.Error.NotFound)
                }

                //remote
                val firstErrorTracker: (Int, Throwable) -> Unit = { i, ex ->
                    if (i == 1) {//on first retry, send a partial result based on the local result
                        launch {
                            sender.send(SelectVersetInteractor.Response.Partial(verset, ex))
                        }
                    }
                }
                val props = retry(3, tracker = firstErrorTracker) {
                    withContext(coroutineContext + dispatchers.IO) {
                        repository.getVersetProps(versetKey)
                    }
                }

                sender.sendAndClose(
                    SelectVersetInteractor.Response.OK(
                        verset.copy(isFavorite = props.isFavorite, tags = props.tags)
                    )
                )
                Unit
            }
            Unit

        }

    private class ErrorResponseAdapter(val errorResponse: SelectVersetInteractor.Response.Error) :
        Throwable()
}