/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.interactors.setup

import com.crskdev.biblereaderplus.common.util.retryWhen
import com.crskdev.biblereaderplus.common.util.switchSelectOnReceive
import com.crskdev.biblereaderplus.domain.entity.DeviceAccountCredential
import com.crskdev.biblereaderplus.domain.gateway.*
import com.crskdev.biblereaderplus.domain.interactors.setup.SetupInteractor.Request
import com.crskdev.biblereaderplus.domain.interactors.setup.SetupInteractor.Response
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface SetupInteractor {
    suspend fun request(request: ReceiveChannel<Request>, response: (Response) -> Unit)
    sealed class Request {
        object Check : SetupInteractor.Request()
        class AuthPromptSelection(val deviceAccountCredential: DeviceAccountCredential) :
            SetupInteractor.Request()

        object Retry : SetupInteractor.Request()
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

        sealed class SynchStep : SetupInteractor.Response() {
            object Prepare : SetupInteractor.Response.SynchStep(), SetupInteractor.Step
            object NeedPermission : SetupInteractor.Response.SynchStep(), SetupInteractor.StepState
            object Authenticating : SetupInteractor.Response.SynchStep(), SetupInteractor.StepState
            object Synchronizing : SetupInteractor.Response.SynchStep(), SetupInteractor.StepState
            object Done : SetupInteractor.Response.SynchStep(), SetupInteractor.StepState
            @Suppress("unused")
            class Error(val errMessage: String? = null) : SetupInteractor.Response.SynchStep(),
                SetupInteractor.StepState
        }

        object Finished : SetupInteractor.Response(), SetupInteractor.Step
        @Suppress("unused")
        class Error(val errorMessage: String?) : SetupInteractor.Response()

        class RetryableError(val errorMessage: String?) : SetupInteractor.Response()
    }
}

/**
 * Created by Cristian Pela on 06.11.2018.
 */
@ObsoleteCoroutinesApi
class SetupInteractorImpl @Inject constructor(
    private val dispatchers: GatewayDispatchers,
    private val setupCheckService: SetupCheckService,
    private val authService: AuthService,
    private val downloadDocumentService: DownloadDocumentService,
    private val documentRepository: DocumentRepository,
    private val remoteDocumentRepository: RemoteDocumentRepository) : SetupInteractor {

    override suspend fun request(request: ReceiveChannel<Request>, response: (Response) -> Unit) =
        coroutineScope {
            val sendChannel = actor<Response> {
                for (r in channel) {
                    response(r)
                }
            }
            switchSelectOnReceive(request) { _, r ->
                when (r) {
                    is Request.Check,
                    is Request.Retry ->
                        handleRequestCheckRetry(sendChannel)
                    is Request.AuthPromptSelection ->
                        handleRequestAuthPrompt(r.deviceAccountCredential, sendChannel)
                }
            }
            Unit
        }

    private suspend fun handleRequestCheckRetry(channel: SendChannel<Response>) = coroutineScope {
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
            is SetupCheckService.Step.Synch -> {
                resumeFromSync(channel)
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

    private suspend fun handleRequestAuthPrompt(credential: DeviceAccountCredential, channel: SendChannel<Response>) {
        resumeFromAuth(credential, channel)
    }

    private suspend fun sendErrorResponse(channel: SendChannel<Response>, err: Response.Error) {
        channel.send(err)
    }

    private suspend fun resumeFromInitialized(channel: SendChannel<Response>) {
        channel.send(Response.Initialized)
    }

    private suspend fun resumeFromFinished(channel: SendChannel<Response>) = coroutineScope {
        channel.send(Response.Finished)
        withContext(coroutineContext + dispatchers.DEFAULT) {
            setupCheckService.next(SetupCheckService.Step.Initialized)
        }
    }

    private suspend fun resumeFromDownload(channel: SendChannel<Response>) =
        coroutineScope {
            channel.send(Response.DownloadStep.Prepare)

            val documentResponse = retryWhen(
                times = 3,
                retryWhen = { _, r -> r is DownloadDocumentService.Error }) {
                withContext(coroutineContext + dispatchers.IO) {
                    downloadDocumentService.download()
                }
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
                    withContext(coroutineContext + dispatchers.DEFAULT) {
                        documentRepository.save(documentResponse.document)
                        //transition to auth state
                        setupCheckService.next(SetupCheckService.Step.AuthStep)
                    }
                    channel.send(Response.DownloadStep.Done)
                }
            }

        }

    private suspend fun resumeFromAuth(deviceAccountCredential: DeviceAccountCredential, channel: SendChannel<Response>) =
        coroutineScope {
            channel.send(Response.SynchStep.Prepare)
            when (deviceAccountCredential) {
                is DeviceAccountCredential.Unauthorized -> {
                    if (!authService.isAuthenticated()) {
                        channel.send(Response.SynchStep.Authenticating)
                        authService.requestAuthPermission()
                    } else {
                        channel.send(Response.SynchStep.Authenticating)
                        val (error, success) = withContext(coroutineContext + dispatchers.DEFAULT) {
                            withContext(coroutineContext + dispatchers.IO) {
                                authService.authenticateWithPermissionGranted()
                            }
                        }
                        if (success) {
                            channel.send(Response.SynchStep.Synchronizing)
                            withContext(coroutineContext + dispatchers.IO) {
                                setupCheckService.next(SetupCheckService.Step.Synch)
                                resumeFromSync(channel)
                            }
                            channel.send(Response.SynchStep.Done)
                            resumeFromFinished(channel)
                        } else {
                            channel.send(Response.SynchStep.Error(error?.message))
                        }
                    }
                }
                is DeviceAccountCredential.AuthorizationPayload -> {
                    channel.send(Response.SynchStep.Authenticating)
                    val (error, success) = withContext(coroutineContext + dispatchers.DEFAULT) {
                        authService.authenticate(deviceAccountCredential.credentialData)
                    }
                    if (success) {
                        channel.send(Response.SynchStep.Synchronizing)
                        withContext(coroutineContext + dispatchers.IO) {
                            setupCheckService.next(SetupCheckService.Step.Synch)
                            resumeFromSync(channel)
                        }
                        channel.send(Response.SynchStep.Done)
                        resumeFromFinished(channel)
                    } else {
                        channel.send(Response.SynchStep.Error(error?.message))
                    }
                }
            }
        }

    private suspend fun resumeFromSync(channel: SendChannel<SetupInteractor.Response>) =
        coroutineScope {
            channel.send(Response.SynchStep.Synchronizing)
            val tagsPromise = async(coroutineContext + dispatchers.IO) {
                remoteDocumentRepository.getAllTags()
            }
            val favoritesPromise = async(coroutineContext + dispatchers.IO) {
                remoteDocumentRepository.getAllFavorites()
            }
            withContext(coroutineContext + dispatchers.DEFAULT) {
                //TODO do it in transaction?
                documentRepository.tagCreate(*tagsPromise.await().toTypedArray())
                val favorites = favoritesPromise.await()
                documentRepository.favoriteActionBatch(true, *favorites.map { it.id }.toIntArray())
                favorites.forEach {
                    documentRepository.tagFavoriteVersetBatch(
                        true,
                        it.id,
                        *it.tagIds.toTypedArray()
                    )
                }
            }
            setupCheckService.next(SetupCheckService.Step.Initialized)
            Unit
        }

    private suspend fun resumeFromUninitialized(channel: SendChannel<SetupInteractor.Response>) =
        coroutineScope {
            channel.send(Response.DownloadStep.Prepare)
            withContext(coroutineContext + dispatchers.DEFAULT) {
                setupCheckService.next(SetupCheckService.Step.DownloadStep)
            }
        }
}