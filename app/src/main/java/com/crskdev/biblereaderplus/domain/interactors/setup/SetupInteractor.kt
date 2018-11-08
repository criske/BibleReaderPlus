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
            is Request.Check,
            is Request.Retry ->
                handleRequestCheckRetry(request.responseChannel)
            is Request.AuthPrompt ->
                handleRequestAuthPrompt(request.deviceAccountCredential, request.responseChannel)
        }
        request.responseChannel.close()
    }

    private suspend fun handleRequestCheckRetry(channel: Channel<Response>) {
        val existentStep = withContext(dispatchers.DEFAULT) {
            setupCheckService.getStep()
        }
        when (existentStep) {
            is SetupCheckService.Step.Uninitialized -> {
                resumeFromUninitialized(channel)
            }
            is SetupCheckService.Step.DownloadStep -> {
                resumeFromDownload(channel)
            }
            is SetupCheckService.Step.AuthStep -> {
                resumeFromAuth(
                    DeviceAccountCredential.Unauthorized,
                    channel
                )
            }
            is SetupCheckService.Step.Finished -> {
                resumeFromFinished(channel)
            }
            is SetupCheckService.Step.Initialized -> {
                resumeFromInitialized(channel)
            }
            is SetupCheckService.Step.Error -> {
                sendErrorResponse(
                    channel,
                    Response.Error(existentStep.err.message)
                )
            }
        }
    }

    private suspend fun handleRequestAuthPrompt(credential: DeviceAccountCredential, channel: Channel<Response>) {
        resumeFromAuth(credential, channel)
    }

    private suspend fun sendErrorResponse(channel: Channel<Response>, err: Response.Error) {
        channel.send(err)
    }

    private suspend fun resumeFromInitialized(channel: Channel<Response>) {
        channel.send(Response.Initialized)
    }

    private suspend fun resumeFromFinished(channel: Channel<Response>) {
        channel.send(Response.Finished)
        withContext(dispatchers.DEFAULT) {
            setupCheckService.next(SetupCheckService.Step.Initialized)
        }
    }

    private suspend fun resumeFromDownload(channel: Channel<Response>) =
        coroutineScope {
            channel.send(Response.DownloadStep.Prepare)

            val documentResponse = withContext(dispatchers.IO) {
                downloadDocumentService.download()
            }

            when (documentResponse) {
                is DownloadDocumentService.Response.ErrorResponse -> {
                    val err = documentResponse.error
                    when (err) {
                        is DownloadDocumentService.Error.Http -> {
                            val code = err.code
                            when (code) {
                                404 -> channel.send(Response.DownloadStep.Error.NotFound)
                                408 -> channel.send(Response.DownloadStep.Error.Timeout)
                                else -> channel.send(Response.DownloadStep.Error.Other(err.message))
                            }
                        }
                        is DownloadDocumentService.Error.Network -> {
                            channel.send(Response.DownloadStep.Error.Network)
                        }
                        is DownloadDocumentService.Error.Conversion,
                        is DownloadDocumentService.Error.Unexpected -> {
                            channel.send(Response.DownloadStep.Error.Other(err.message))
                        }
                    }
                }
                is DownloadDocumentService.Response.OKResponse -> {
                    channel.send(Response.DownloadStep.Persist)
                    withContext(dispatchers.DEFAULT) {
                        documentRepository.save(documentResponse.document)
                        //transition to auth state
                        setupCheckService.next(SetupCheckService.Step.AuthStep)
                    }
                    channel.send(Response.DownloadStep.Done)
                }
            }

        }

    private suspend fun resumeFromAuth(deviceAccountCredential: DeviceAccountCredential, channel: Channel<Response>) =
        coroutineScope {
            channel.send(Response.AuthStep.Prepare)
            when (deviceAccountCredential) {
                is DeviceAccountCredential.Unauthorized -> {
                    if (!authService.hasPermission()) {
                        channel.send(Response.AuthStep.NeedPermission)
                        authService.requestPermission()
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
                    }
                }
            }
        }

    private suspend fun resumeFromUninitialized(channel: Channel<Response>) =
        coroutineScope {
            channel.send(SetupInteractor.Response.DownloadStep.Prepare)
            withContext(dispatchers.DEFAULT) {
                setupCheckService.next(SetupCheckService.Step.DownloadStep)
            }
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