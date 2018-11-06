/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.setup

import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.DownloadDocumentService
import com.crskdev.biblereaderplus.domain.gateway.SetupCheckService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Created by Cristian Pela on 06.11.2018.
 */
class SetupInteractor @Inject constructor(
    private val setupCheckService: SetupCheckService,
    private val downloadDocumentService: DownloadDocumentService,
    private val documentRepository: DocumentRepository) {

    suspend fun request(request: Request): ReceiveChannel<Response> = coroutineScope {
        val responseChannel = Channel<Response>()
        when (request) {
            is Request.Check -> {
                val existentStep = async(Dispatchers.Unconfined) { setupCheckService.getStep() }
                when (existentStep.await()) {
                    is SetupCheckService.Step.Uninitialized -> {
                        responseChannel.send(SetupInteractor.Response.DownloadStep.Prepare(100))
                        val next =
                            async { setupCheckService.next(SetupCheckService.Step.DownloadStep) }
                    }
                }
            }
            else -> throw Error()
        }
        responseChannel
    }

    sealed class Request {
        object Check : Request()
        object Retry : Request()
    }

    sealed class Response {
        object None : Response()
        object Initialized : Response()
        sealed class DownloadStep : Response() {
            class Prepare(val total: Long) : DownloadStep()
            class Progress(val position: Long, val total: Long)
            object Done : DownloadStep()
        }

        object AuthStep : Response()
        object Finished : Response()
        sealed class Error(val errorMessage: String) {
            class IOError(errMessage: String) : Error(errMessage)
            class AuthError(errMessage: String) : Error(errMessage)
        }

    }
}