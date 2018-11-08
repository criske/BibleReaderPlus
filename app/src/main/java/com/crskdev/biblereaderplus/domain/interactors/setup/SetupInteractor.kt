/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.setup

import com.crskdev.biblereaderplus.domain.entity.DeviceAccountCredential
import com.crskdev.biblereaderplus.domain.gateway.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Created by Cristian Pela on 06.11.2018.
 */
class SetupInteractor @Inject constructor(
    private val dispatchers: GatewayDispatchers,
    private val setupCheckService: SetupCheckService,
    private val authService: AuthService,
    private val downloadDocumentService: DownloadDocumentService,
    private val documentRepository: DocumentRepository) {

    suspend fun request(request: Request) = coroutineScope {

        request.responseChannel.close()
    }


    sealed class Request(val responseChannel: Channel<Response>) {
        class Check(responseChannel: Channel<Response>) : Request(responseChannel)
        class AuthPrompt(val deviceAccountCredential: DeviceAccountCredential, responseChannel: Channel<Response>) :
            Request(responseChannel)

        class Retry(responseChannel: Channel<Response>) : Request(responseChannel)
    }

    interface StepState
    interface Step
    sealed class Response {
        object Initialized : Response(), Step
        sealed class DownloadStep : Response() {
            object Prepare : DownloadStep(), Step
            object Persist : DownloadStep(), StepState
            object Done : DownloadStep(), StepState
            sealed class Error : DownloadStep(), StepState {
                object Network : Error()
                object Timeout : Error()
                object NotFound : Error()
                class Other(val message: String?) : Error()
            }
        }

        sealed class AuthStep : Response() {
            object Prepare : AuthStep(), Step
            object NeedPermission : AuthStep(), StepState
            object Authenticating : AuthStep(), StepState
            object Done : AuthStep(), StepState
            class Error(val errMessage: String?) : AuthStep(), StepState
        }

        object Finished : Response(), Step
        class Error(val errorMessage: String?) : Response()
    }
}