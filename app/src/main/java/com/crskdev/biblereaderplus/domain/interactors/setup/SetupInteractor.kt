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

interface SetupInteractor {
    suspend fun request(request: SetupInteractor.Request): Boolean
    sealed class Request(val responseChannel: Channel<SetupInteractor.Response>) {
        class Check(responseChannel: Channel<SetupInteractor.Response>) : SetupInteractor.Request(responseChannel)
        class AuthPrompt(val deviceAccountCredential: DeviceAccountCredential, responseChannel: Channel<SetupInteractor.Response>) :
            SetupInteractor.Request(responseChannel)

        class Retry(responseChannel: Channel<SetupInteractor.Response>) : SetupInteractor.Request(responseChannel)
    }

    interface StepState
    interface Step
    sealed class Response {
        object Initialized : SetupInteractor.Response(), SetupInteractor.Step
        sealed class DownloadStep : SetupInteractor.Response() {
            object Prepare : SetupInteractor.Response.DownloadStep(), SetupInteractor.Step
            object Persist : SetupInteractor.Response.DownloadStep(), SetupInteractor.StepState
            object Done : SetupInteractor.Response.DownloadStep(), SetupInteractor.StepState
            sealed class Error : SetupInteractor.Response.DownloadStep(),
                SetupInteractor.StepState {
                object Network : SetupInteractor.Response.DownloadStep.Error()
                object Timeout : SetupInteractor.Response.DownloadStep.Error()
                object NotFound : SetupInteractor.Response.DownloadStep.Error()
                class Other(val message: String?) : SetupInteractor.Response.DownloadStep.Error()
            }
        }

        sealed class AuthStep : SetupInteractor.Response() {
            object Prepare : SetupInteractor.Response.AuthStep(), SetupInteractor.Step
            object NeedPermission : SetupInteractor.Response.AuthStep(), SetupInteractor.StepState
            object Authenticating : SetupInteractor.Response.AuthStep(), SetupInteractor.StepState
            object Done : SetupInteractor.Response.AuthStep(), SetupInteractor.StepState
            class Error(val errMessage: String?) : SetupInteractor.Response.AuthStep(),
                SetupInteractor.StepState
        }

        object Finished : SetupInteractor.Response(), SetupInteractor.Step
        class Error(val errorMessage: String?) : SetupInteractor.Response()
    }
}

/**
 * Created by Cristian Pela on 06.11.2018.
 */
class SetupInteractorImpl @Inject constructor(
    private val dispatchers: GatewayDispatchers,
    private val setupCheckService: SetupCheckService,
    private val authService: AuthService,
    private val downloadDocumentService: DownloadDocumentService,
    private val documentRepository: DocumentRepository) : SetupInteractor {

    override suspend fun request(request: SetupInteractor.Request) = coroutineScope {
        request.responseChannel.close()
    }


}