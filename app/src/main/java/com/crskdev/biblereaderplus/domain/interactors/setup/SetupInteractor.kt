/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2020.
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
        object Check : Request()
        class AuthPromptSelection(val deviceAccountCredential: DeviceAccountCredential) :
            Request()

        object Retry : Request()
    }

    interface StepState
    interface Step
    sealed class Response {
        object Initialized : Response(), Step
        sealed class DownloadStep : Response() {
            object Prepare : Response.DownloadStep(), Step
            object Persist : Response.DownloadStep(), StepState
            object Done : Response.DownloadStep(), StepState
//            sealed class Error : SetupInteractor.Response.DownloadStep(),
//                SetupInteractor.StepState {
//                object Network : SetupInteractor.Response.DownloadStep.Error()
//                object Timeout : SetupInteractor.Response.DownloadStep.Error()
//                object NotFound : SetupInteractor.Response.DownloadStep.Error()
//                class Other(val message: String?) : SetupInteractor.Response.DownloadStep.Error()
//            }
        }

        sealed class SynchStep : Response() {
            object Prepare : Response.SynchStep(), Step
            object NeedPermission : Response.SynchStep(), StepState
            object Authenticating : Response.SynchStep(), StepState
            object Synchronizing : Response.SynchStep(), StepState
            object Done : Response.SynchStep(), StepState
//            @Suppress("unused")
//            class Error(val errMessage: String? = null) : SetupInteractor.Response.SynchStep(),
//                SetupInteractor.StepState
        }

        object Finished : Response(), Step


        sealed class Error(val message: String? = null, val code: Int = UNKNOWN_CODE) : Response() {
            companion object {
                const val UNKNOWN_CODE = -1
            }

            class Once(message: String?, code: Int = UNKNOWN_CODE) :
                Response.Error(message, code)

            class Retryable(message: String?, code: Int = UNKNOWN_CODE) :
                Response.Error(message, code)
        }

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
    private val remoteDocumentRepository: RemoteDocumentRepository,
    private val dateFormatter: DateFormatter) : SetupInteractor {

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

    private suspend fun handleRequestCheckRetry(channel: SendChannel<Response>): Unit =
        coroutineScope {
            val existentStep = withContext(dispatchers.DEFAULT) {
                setupCheckService.getStep()
            }
            nextState(existentStep, channel)
        }

    private suspend fun handleRequestAuthPrompt(credential: DeviceAccountCredential, channel: SendChannel<Response>) {
        resumeFromAuth(credential, channel)
    }

    private suspend fun resumeFromInitialized(channel: SendChannel<Response>) {
        channel.send(Response.Initialized)
    }

    private suspend fun resumeFromFinished(channel: SendChannel<Response>) = coroutineScope {
        withContext(dispatchers.DEFAULT) {
            setupCheckService.save(SetupCheckService.Step.INITIALIZED)
        }
        channel.send(Response.Finished)
    }

    private suspend fun resumeFromDownload(channel: SendChannel<Response>) =
        coroutineScope {
            channel.send(Response.DownloadStep.Prepare)
            withContext(dispatchers.DEFAULT) {
                setupCheckService.save(SetupCheckService.Step.DOWNLOAD)
            }
            val documentResponse = retryWhen(
                times = 3,
                retryWhen = { _, r -> r is DownloadDocumentService.Error }) {
                withContext(coroutineContext + dispatchers.IO) {
                    downloadDocumentService.download()
                }
            }

            when (documentResponse) {
                is DownloadDocumentService.Response.ErrorResponse -> {
                    when (documentResponse.error) {
                        is DownloadDocumentService.Error.Http -> {
                            when (documentResponse.error.code) {
                                404 -> channel.send(
                                    Response.Error.Once(
                                        documentResponse.error.message,
                                        documentResponse.error.code
                                    )
                                )
                                408 -> channel.send(
                                    Response.Error.Retryable(
                                        documentResponse.error.message,
                                        documentResponse.error.code
                                    )
                                )
                                else -> channel.send(
                                    Response.Error.Once(
                                        documentResponse.error.message,
                                        documentResponse.error.code
                                    )
                                )
                            }
                        }
                        is DownloadDocumentService.Error.Network -> {
                            channel.send(Response.Error.Retryable(documentResponse.error.message))
                        }
                        is DownloadDocumentService.Error.Conversion,
                        is DownloadDocumentService.Error.Unexpected -> {
                            channel.send(Response.Error.Once(documentResponse.error.message))
                        }
                    }
                    withContext(dispatchers.DEFAULT) {
                        setupCheckService.save(SetupCheckService.Step.DOWNLOAD.previous())
                    }
                }
                is DownloadDocumentService.Response.OKResponse -> {
                    channel.send(Response.DownloadStep.Persist)
                    withContext(dispatchers.DEFAULT) {
                        documentRepository.save(documentResponse.document)
                    }
                    channel.send(Response.DownloadStep.Done)
                    nextState(SetupCheckService.Step.DOWNLOAD, channel)
                }
            }
            Unit

        }

    private suspend fun resumeFromAuth(deviceAccountCredential: DeviceAccountCredential = DeviceAccountCredential.Unauthorized,
                                       channel: SendChannel<Response>) =
        coroutineScope {
            channel.send(Response.SynchStep.Authenticating)
            withContext(dispatchers.DEFAULT) {
                setupCheckService.save(SetupCheckService.Step.AUTH)
            }
            when (deviceAccountCredential) {
                is DeviceAccountCredential.Unauthorized -> {
                    //if (!authService.isAuthenticated()) {
                        channel.send(Response.SynchStep.Authenticating)
                        channel.send(Response.SynchStep.NeedPermission)
                        authService.requestAuthPermission()
//                    } else {
//                        val (error, success) = withContext(dispatchers.DEFAULT) {
//                            withContext(dispatchers.IO) {
//                                authService.authenticateWithPermissionGranted()
//                            }
//                        }
//                        if (success) {
//                            nextState(SetupCheckService.Step.AUTH, channel)
//                        } else {
//                            channel.send(Response.Error.Retryable(error?.message))
//                            withContext(dispatchers.DEFAULT) {
//                                setupCheckService.save(SetupCheckService.Step.AUTH.previous())
//                            }
//                        }
//                    }
                }
                is DeviceAccountCredential.AuthorizationPayload -> {
                    channel.send(Response.SynchStep.Authenticating)
                    val (error, success) = withContext(dispatchers.DEFAULT) {
                        authService.authenticate(deviceAccountCredential.credentialData)
                    }
                    if (success) {
                        nextState(SetupCheckService.Step.AUTH, channel)
                    } else {
                        channel.send(Response.Error.Retryable(error?.message))
                        withContext(dispatchers.DEFAULT) {
                            setupCheckService.save(SetupCheckService.Step.AUTH.previous())
                        }
                    }
                }
            }
        }

    private suspend fun resumeFromSync(channel: SendChannel<Response>) =
        coroutineScope {
            channel.send(Response.SynchStep.Prepare)
            channel.send(Response.SynchStep.Synchronizing)
            withContext(dispatchers.DEFAULT) {
                setupCheckService.save(SetupCheckService.Step.SYNCH)
            }
            val tagsPromise = async(dispatchers.IO) {
                remoteDocumentRepository.getAllTags()
            }
            val favoritesPromise = async(dispatchers.IO) {
                remoteDocumentRepository.getAllFavorites()
            }
            withContext(dispatchers.DEFAULT) {
                val tags = tagsPromise.await()
                val favorites = favoritesPromise.await()
                documentRepository.runTransaction {
                    tagCreate(*tags.toTypedArray())
                    favoriteActionBatch(
                        true,
                        *favorites.map { it.id to it.modifiedAt }.toTypedArray()
                    )
                    favorites.forEach {
                        tagFavoriteVersetBatch(
                            true,
                            it.id,
                            it.modifiedAt,
                            *it.tagIds.toTypedArray()
                        )
                    }
                }
            }
            channel.send(Response.SynchStep.Done)
            nextState(SetupCheckService.Step.SYNCH, channel)
            Unit
        }

    private suspend fun nextState(from: SetupCheckService.Step, channel: SendChannel<Response>): Unit =
        coroutineScope {
            when (from) {
                SetupCheckService.Step.INITIALIZED -> resumeFromInitialized(channel)
                SetupCheckService.Step.UNINITIALIZED -> resumeFromDownload(channel)
                SetupCheckService.Step.DOWNLOAD -> resumeFromAuth(channel = channel)
                SetupCheckService.Step.AUTH -> resumeFromSync(channel)
                SetupCheckService.Step.SYNCH -> resumeFromFinished(channel)
                else -> {
                    //no-op
                }
            }
            Unit
        }

}