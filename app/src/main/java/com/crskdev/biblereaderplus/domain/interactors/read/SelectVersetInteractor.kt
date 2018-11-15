package com.crskdev.biblereaderplus.domain.interactors.read

import com.crskdev.biblereaderplus.domain.entity.SelectedVerset
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor

/**
 * Created by Cristian Pela on 13.11.2018.
 */
interface SelectVersetInteractor {

    suspend fun request(versetKey: VersetKey, response: (Response) -> Unit)

    sealed class Response {
        class OK(val result: SelectedVerset) : Response()
        object Wait : Response()
        sealed class Error : Response() {
            class Generic(val throwable: Throwable) : Error()
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

            println("Send wait from ${Thread.currentThread()}")
            response(SelectVersetInteractor.Response.Wait)

            val sendChannel = actor<SelectVersetInteractor.Response> {
                for (r in channel) {
                    response(r)
                }
            }

            val errHandler = coroutineContext + CoroutineExceptionHandler { _, throwable ->
                launch(dispatchers.MAIN) {
                    when (throwable) {
                        is ErrorResponseAdapter -> sendChannel.send(throwable.errorResponse)
                        else -> sendChannel.send(
                            SelectVersetInteractor.Response.Error.Generic(throwable)
                        )
                    }
                }
                sendChannel.close()
            }

            launch(errHandler + dispatchers.DEFAULT) {
                println("Get verset from ${Thread.currentThread()}")
                val verset = repository.getVerset(versetKey)
                    ?: throw ErrorResponseAdapter(SelectVersetInteractor.Response.Error.NotFound)
                val props = withContext(errHandler + dispatchers.IO) {
                    println("Get verset props from ${Thread.currentThread()}")
                    repository.getVersetProps(versetKey)
                }
                launch(dispatchers.MAIN) {
                    println("Send final result from ${Thread.currentThread()}")
                    sendChannel.send(
                        SelectVersetInteractor.Response
                            .OK(verset.copy(isFavorite = props.isFavorite, tags = props.tags))
                    )
                    sendChannel.close()
                }
            }
            Unit
        }

    private class ErrorResponseAdapter(val errorResponse: SelectVersetInteractor.Response.Error) :
        Throwable()
}