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

            val sender = actor<SelectVersetInteractor.Response> {
                for (r in channel) {
                    response(r)
                }
            }

            sender.send(SelectVersetInteractor.Response.Wait)

            supervisorScope {

                val errHandler = coroutineContext + CoroutineExceptionHandler { ctx, throwable ->
                    runBlocking(ctx + dispatchers.MAIN) {
                        when (throwable) {
                            is ErrorResponseAdapter -> sender.send(throwable.errorResponse)
                            else -> sender.send(SelectVersetInteractor.Response.Error.Generic(throwable))
                        }
                    }
                    sender.close()
                }

                launch(errHandler + dispatchers.DEFAULT) {

                    val verset = repository.getVerset(versetKey)
                        ?: throw ErrorResponseAdapter(SelectVersetInteractor.Response.Error.NotFound)

                    val props = withContext(dispatchers.IO) {
                        repository.getVersetProps(versetKey)
                    }

                    with(sender) {
                        send(
                            SelectVersetInteractor.Response.OK(
                                verset.copy(isFavorite = props.isFavorite, tags = props.tags)
                            )
                        )
                        close()
                        Unit
                    }
                }
                Unit
            }
            Unit
        }

    private class ErrorResponseAdapter(val errorResponse: SelectVersetInteractor.Response.Error) :
        Throwable()
}