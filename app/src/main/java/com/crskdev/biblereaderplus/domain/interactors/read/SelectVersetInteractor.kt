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
