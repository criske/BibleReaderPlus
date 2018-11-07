/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.setup

import com.crskdev.biblereaderplus.domain.entity.DeviceAccountCredential
import com.crskdev.biblereaderplus.domain.gateway.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
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
        when (request) {
            is Request.Check -> {
                val existentStep = withContext(dispatchers.DEFAULT) { setupCheckService.getStep() }
                when (existentStep) {
                    is SetupCheckService.Step.Uninitialized -> {
                        resumeFromUninitialized(request.responseChannel)
                    }
                    is SetupCheckService.Step.DownloadStep -> {
                        resumeFromDownload(request.responseChannel)
                    }
                    is SetupCheckService.Step.AuthStep -> {
                        resumeFromAuth(
                            DeviceAccountCredential.Unauthorized,
                            request.responseChannel
                        )
                    }
                    is SetupCheckService.Step.Finished -> {
                        resumeFromFinished(request.responseChannel)
                    }
                    is SetupCheckService.Step.Initialized -> {
                        resumeFromInitialized(request.responseChannel)
                    }
                    is SetupCheckService.Step.Error -> {
                        sendErrorResponse(
                            request.responseChannel,
                            Response.Error(existentStep.err.message)
                        )
                    }
                }
            }
            is Request.AuthPrompt -> {
                resumeFromAuth(request.deviceAccountCredential, request.responseChannel)
            }
            else -> throw Error()
        }
    }

    private suspend fun sendErrorResponse(channel: Channel<Response>, err: Response.Error) {
        channel.send(err)
        channel.close()
    }

    private suspend fun resumeFromInitialized(channel: Channel<Response>) {
        channel.send(Response.Initialized)
        channel.close()
    }

    private suspend fun resumeFromFinished(channel: Channel<Response>) {
        channel.send(Response.Finished)
        withContext(dispatchers.DEFAULT) {
            setupCheckService.next(SetupCheckService.Step.Initialized)
        }
        channel.close()
    }

    private suspend fun resumeFromDownload(channel: Channel<Response>) {
        withContext(dispatchers.DEFAULT) {
            setupCheckService.next(SetupCheckService.Step.AuthStep)
        }
        channel.close()
    }

    private suspend fun resumeFromAuth(deviceAccountCredential: DeviceAccountCredential, channel: Channel<Response>) {
        channel.send(Response.AuthStep.Prepare)
        when (deviceAccountCredential) {
            is DeviceAccountCredential.Unauthorized -> {
                if (!authService.hasPermission()) {
                    channel.send(Response.AuthStep.NeedPermission)
                    authService.requestPermission()
                    channel.close()
                } else {
                    channel.send(Response.AuthStep.Authenticating)
                    val (error, success) = withContext(dispatchers.DEFAULT) {
                        authService.authenticateWithPermissionGranted()
                    }
                    if (success) {
                        channel.send(Response.AuthStep.Done)
                        resumeFromFinished(channel)
                    } else {
                        channel.send(Response.AuthStep.Error(error?.message))
                        channel.close()
                    }
                }

            }
            is DeviceAccountCredential.AuthorizationPayload -> {
                channel.send(Response.AuthStep.Authenticating)
                val (error, success) = withContext(dispatchers.DEFAULT) {
                    authService.authenticate(deviceAccountCredential.credentialData)
                }
                if (success) {
                    channel.send(Response.AuthStep.Done)
                    resumeFromFinished(channel)
                } else {
                    channel.send(Response.AuthStep.Error(error?.message))
                    channel.close()
                }
            }
        }
    }

    private suspend fun resumeFromUninitialized(channel: Channel<Response>) {
        channel.send(SetupInteractor.Response.DownloadStep.Prepare)
        withContext(dispatchers.DEFAULT) {
            setupCheckService.next(SetupCheckService.Step.DownloadStep)
        }
        channel.close()
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
            data class Progress(val position: Long, val total: Long) : DownloadStep(), StepState
            object Persist : DownloadStep(), StepState
            object Done : DownloadStep(), StepState
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