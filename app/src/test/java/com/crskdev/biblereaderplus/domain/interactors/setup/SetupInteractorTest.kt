/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.setup

import com.crskdev.biblereaderplus.domain.entity.DeviceAccountCredential
import com.crskdev.biblereaderplus.domain.entity.Document
import com.crskdev.biblereaderplus.domain.gateway.AuthService
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.DownloadDocumentService
import com.crskdev.biblereaderplus.domain.gateway.SetupCheckService
import com.crskdev.biblereaderplus.testutil.TestDispatchers
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Created by Cristian Pela on 06.11.2018.
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class SetupInteractorTest {

    @MockK
    lateinit var setupService: SetupCheckService
    @MockK
    lateinit var downloadDocService: DownloadDocumentService
    @MockK
    lateinit var docRepository: DocumentRepository

    @MockK
    lateinit var authService: AuthService

    lateinit var setupInteractor: SetupInteractor

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        setupInteractor = SetupInteractorImpl(
            TestDispatchers,
            setupService,
            authService,
            downloadDocService,
            docRepository
        )
    }

    @Test
    fun `when request check step is initialized should respond with initialized`() {

        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.Step.Initialized

            val responseChannel = actor<SetupInteractor.Response>() {
                assertEquals(
                    listOf(SetupInteractor.Response.Initialized),
                    channel.toCollection(mutableListOf())
                )
                verify(exactly = 0) { setupService.next(SetupCheckService.Step.DownloadStep) }
            }
            launch {
                setupInteractor.request(SetupInteractor.Request.Check(responseChannel))
            }


        }
    }

    @Test
    fun `when request check is uninitialized should respond with download step`() {

        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.Step.Uninitialized

            val responseChannel = actor<SetupInteractor.Response>() {
                assertEquals(
                    listOf(SetupInteractor.Response.DownloadStep.Prepare),
                    channel.toCollection(mutableListOf())
                )
                verify { setupService.next(SetupCheckService.Step.DownloadStep) }

            }
            launch {
                setupInteractor.request(SetupInteractor.Request.Check(responseChannel))
            }

        }
    }

    @Test
    fun `when request check is auth and no permission should return need permission`() {

        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.Step.AuthStep
            every { authService.hasPermission() } returns false
            coEvery { authService.authenticate(any()) } returns Pair<Error?, Boolean>(null, true)
            coEvery { authService.authenticateWithPermissionGranted() } returns Pair<Error?, Boolean>(
                null,
                true
            )
            val responseChannel = actor<SetupInteractor.Response> {
                assertEquals(
                    listOf(
                        SetupInteractor.Response.AuthStep.Prepare,
                        SetupInteractor.Response.AuthStep.NeedPermission
                    ),
                    channel.toCollection(mutableListOf())
                )
                verify { authService.requestPermission() }
            }
            launch {
                setupInteractor.request(SetupInteractor.Request.Check(responseChannel))
            }
        }

    }

    @Test
    fun `when request check is auth and right after permission granted should authenticate with provided auth payload `() {

        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.Step.AuthStep
            every { authService.hasPermission() } returns true
            coEvery { authService.authenticate(any()) } returns Pair<Error?, Boolean>(null, true)

            val responseChannel = actor<SetupInteractor.Response>() {
                assertEquals(
                    listOf(
                        SetupInteractor.Response.AuthStep.Prepare,
                        SetupInteractor.Response.AuthStep.Authenticating,
                        SetupInteractor.Response.AuthStep.Done,
                        SetupInteractor.Response.Finished
                    ),
                    channel.toCollection(mutableListOf())
                )
                coVerify { authService.authenticate(any()) }
            }
            launch {
                setupInteractor.request(
                    SetupInteractor.Request.AuthPrompt(
                        DeviceAccountCredential.AuthorizationPayload(
                            Unit
                        ), responseChannel
                    )
                )
            }

        }
    }

    @Test
    fun `when request check is auth and already permission granted should authenticate `() {

        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.Step.AuthStep
            every { authService.hasPermission() } returns true
            coEvery { authService.authenticateWithPermissionGranted() } returns Pair<Error?, Boolean>(
                null,
                true
            )

            val responseChannel = actor<SetupInteractor.Response>() {
                assertEquals(
                    listOf(
                        SetupInteractor.Response.AuthStep.Prepare,
                        SetupInteractor.Response.AuthStep.Authenticating,
                        SetupInteractor.Response.AuthStep.Done,
                        SetupInteractor.Response.Finished
                    ),
                    channel.toCollection(mutableListOf())
                )
                coVerify { authService.authenticateWithPermissionGranted() }
            }
            launch {
                setupInteractor.request(SetupInteractor.Request.Check(responseChannel))
            }


        }

    }

    @Test
    fun `when request check is download and there is no internet connection should error`() {
        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.Step.DownloadStep
            coEvery { downloadDocService.download() } returns
                    DownloadDocumentService.Response.ErrorResponse(
                        DownloadDocumentService.Error.Network(
                            null
                        )
                    )

            val responseChannel = actor<SetupInteractor.Response> {
                assertEquals(
                    listOf(
                        SetupInteractor.Response.DownloadStep.Prepare,
                        SetupInteractor.Response.DownloadStep.Error.Network
                    ),
                    channel.toCollection(mutableListOf())
                )
            }
            launch {
                setupInteractor.request(SetupInteractor.Request.Check(responseChannel))
            }


        }
    }

    @Test
    fun `when request check is download and there is not found should return error not found`() {
        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.Step.DownloadStep
            coEvery { downloadDocService.download() } returns
                    DownloadDocumentService.Response.ErrorResponse(
                        DownloadDocumentService.Error.Http(
                            404,
                            null
                        )
                    )

            val responseChannel = actor<SetupInteractor.Response>() {
                assertEquals(
                    listOf(
                        SetupInteractor.Response.DownloadStep.Prepare,
                        SetupInteractor.Response.DownloadStep.Error.NotFound
                    ),
                    channel.toCollection(mutableListOf())
                )
            }
            launch {
                setupInteractor.request(SetupInteractor.Request.Check(responseChannel))
            }


        }
    }

    @Test
    fun `when request check is download and there is timeout should return error timeout`() {
        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.Step.DownloadStep
            coEvery { downloadDocService.download() } returns
                    DownloadDocumentService.Response.ErrorResponse(
                        DownloadDocumentService.Error.Http(
                            408,
                            null
                        )
                    )

            val responseChannel = actor<SetupInteractor.Response>() {
                assertEquals(
                    listOf(
                        SetupInteractor.Response.DownloadStep.Prepare,
                        SetupInteractor.Response.DownloadStep.Error.Timeout
                    ),
                    channel.toCollection(mutableListOf())
                )
            }
            launch {
                setupInteractor.request(SetupInteractor.Request.Check(responseChannel))
            }


        }
    }

    @Test
    fun `when request check is download and there is timeout should return error other`() {
        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.Step.DownloadStep
            coEvery { downloadDocService.download() } returns
                    DownloadDocumentService.Response.ErrorResponse(
                        DownloadDocumentService.Error.Conversion(
                            null
                        )
                    )

            val responseChannel = Channel<SetupInteractor.Response>()
            launch {
                setupInteractor.request(SetupInteractor.Request.Check(responseChannel))
            }

            assertEquals(
                listOf(
                    SetupInteractor.Response.DownloadStep.Prepare::class.simpleName,
                    SetupInteractor.Response.DownloadStep.Error.Other::class.simpleName
                ),
                responseChannel.toCollection(mutableListOf()).map { it::class.simpleName }
            )
        }
    }

    @Test
    fun `when request check is download and there is ok should downalod and store document`() {
        runBlocking {

            val document = Document(emptyList())
            every { setupService.getStep() } returns SetupCheckService.Step.DownloadStep
            coEvery { downloadDocService.download() } returns DownloadDocumentService.Response.OKResponse(
                document
            )

            val responseChannel = actor<SetupInteractor.Response> {
                assertEquals(
                    listOf(
                        SetupInteractor.Response.DownloadStep.Prepare,
                        SetupInteractor.Response.DownloadStep.Persist,
                        SetupInteractor.Response.DownloadStep.Done
                    ),
                    channel.toCollection(mutableListOf())
                )
                coVerify { setupService.next(SetupCheckService.Step.AuthStep) }
                coVerify { docRepository.save(document) }
            }
            launch {
                setupInteractor.request(SetupInteractor.Request.Check(responseChannel))
            }
        }
    }
}
