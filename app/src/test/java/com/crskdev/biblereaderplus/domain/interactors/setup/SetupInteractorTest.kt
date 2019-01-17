/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.interactors.setup

import com.crskdev.biblereaderplus.domain.entity.DeviceAccountCredential
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.gateway.AuthService
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.DownloadDocumentService
import com.crskdev.biblereaderplus.domain.gateway.SetupCheckService
import com.crskdev.biblereaderplus.testutil.TestDispatchers
import com.crskdev.biblereaderplus.testutil.classesName
import com.crskdev.biblereaderplus.testutil.collectEmitted
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
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

    private lateinit var setupInteractor: SetupInteractor

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
            every { setupService.getStep() } returns SetupCheckService.StepDeprecated.Initialized
            launch {
                val actual = collectEmitted<SetupInteractor.Response> {
                    setupInteractor.request(SetupInteractor.Request.Check) {
                        add(it)
                    }
                }
                assertEquals(
                    listOf(SetupInteractor.Response.Initialized),
                    actual
                )
                verify(exactly = 0) { setupService.save(SetupCheckService.StepDeprecated.DownloadStep) }
            }
        }
    }


    @Test
    fun `when request check is uninitialized should respond with download step`() {

        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.StepDeprecated.Uninitialized
            launch {
                val actual = collectEmitted<SetupInteractor.Response> {
                    setupInteractor.request(SetupInteractor.Request.Check) {
                        add(it)
                    }

                }
                assertEquals(
                    listOf(SetupInteractor.Response.DownloadStep.Prepare),
                    actual
                )
                verify { setupService.save(SetupCheckService.StepDeprecated.DownloadStep) }
            }

        }
    }

    @Test
    fun `when request check is auth and no permission should return need permission`() {

        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.StepDeprecated.AuthStep
            every { authService.hasPermission() } returns false
            coEvery { authService.authenticate(any()) } returns Pair<Error?, Boolean>(null, true)
            coEvery { authService.authenticateWithPermissionGranted() } returns Pair<Error?, Boolean>(
                null,
                true
            )
            launch {
                val actual = collectEmitted<SetupInteractor.Response> {
                    setupInteractor.request(SetupInteractor.Request.Check) {
                        add(it)
                    }
                }
                assertEquals(
                    listOf(
                        SetupInteractor.Response.SynchStep.Prepare,
                        SetupInteractor.Response.SynchStep.NeedPermission
                    ),
                    actual
                )
                verify { authService.requestPermission() }
            }
        }

    }

    @Test
    fun `when request check is auth and right after permission granted should authenticate with provided auth payload `() {

        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.StepDeprecated.AuthStep
            every { authService.hasPermission() } returns true
            coEvery { authService.authenticate(any()) } returns Pair<Error?, Boolean>(null, true)

            launch {
                val actual = collectEmitted<SetupInteractor.Response> {
                    setupInteractor.request(
                        SetupInteractor.Request.AuthPromptSelection(
                            DeviceAccountCredential.AuthorizationPayload(Unit)
                        )
                    ) {
                        add(it)
                    }
                }
                assertEquals(
                    listOf(
                        SetupInteractor.Response.SynchStep.Prepare,
                        SetupInteractor.Response.SynchStep.Authenticating,
                        SetupInteractor.Response.SynchStep.Synchronizing,
                        SetupInteractor.Response.SynchStep.Done,
                        SetupInteractor.Response.Finished
                    ),
                    actual
                )
                coVerify { authService.authenticate(any()) }

            }

        }
    }

    @Test
    fun `when request check is auth and already permission granted should authenticate and then synchronize with remote database `() {

        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.StepDeprecated.AuthStep
            every { authService.hasPermission() } returns true
            every { authService.authenticateWithPermissionGranted() } returns Pair<Error?, Boolean>(
                null,
                true
            )
            launch {
                val actual = collectEmitted<SetupInteractor.Response> {
                    setupInteractor.request(SetupInteractor.Request.Check) {
                        add(it)
                    }
                }
                assertEquals(
                    listOf(
                        SetupInteractor.Response.SynchStep.Prepare,
                        SetupInteractor.Response.SynchStep.Authenticating,
                        SetupInteractor.Response.SynchStep.Synchronizing,
                        SetupInteractor.Response.SynchStep.Done,
                        SetupInteractor.Response.Finished
                    ),
                    actual
                )
                verify { docRepository.synchronize() }
                verify { authService.authenticateWithPermissionGranted() }
            }


        }

    }

    @Test
    fun `when request check is download and there is no internet connection should error`() {
        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.StepDeprecated.DownloadStep
            coEvery { downloadDocService.download() } returns
                    DownloadDocumentService.Response.ErrorResponse(
                        DownloadDocumentService.Error.Network(
                            null
                        )
                    )
            launch {
                val actual = collectEmitted<SetupInteractor.Response> {
                    setupInteractor.request(SetupInteractor.Request.Check) {
                        add(it)
                    }
                }
                assertEquals(
                    listOf(
                        SetupInteractor.Response.DownloadStep.Prepare,
                        SetupInteractor.Response.DownloadStep.Error.Network
                    ),
                    actual
                )
            }


        }
    }

    @Test
    fun `when request check is download and there is not found should return error not found`() {
        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.StepDeprecated.DownloadStep
            coEvery { downloadDocService.download() } returns
                    DownloadDocumentService.Response.ErrorResponse(
                        DownloadDocumentService.Error.Http(
                            404,
                            null
                        )
                    )
            launch {
                val actual = collectEmitted<SetupInteractor.Response> {
                    setupInteractor.request(SetupInteractor.Request.Check) {
                        add(it)
                    }
                }
                assertEquals(
                    listOf(
                        SetupInteractor.Response.DownloadStep.Prepare,
                        SetupInteractor.Response.DownloadStep.Error.NotFound
                    ),
                    actual
                )
            }


        }
    }

    @Test
    fun `when request check is download and there is timeout should return error timeout`() {
        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.StepDeprecated.DownloadStep
            coEvery { downloadDocService.download() } returns
                    DownloadDocumentService.Response.ErrorResponse(
                        DownloadDocumentService.Error.Http(
                            408,
                            null
                        )
                    )

            launch {
                val actual = collectEmitted<SetupInteractor.Response> {
                    setupInteractor.request(SetupInteractor.Request.Check) {
                        add(it)
                    }
                }
                assertEquals(
                    listOf(
                        SetupInteractor.Response.DownloadStep.Prepare,
                        SetupInteractor.Response.DownloadStep.Error.Timeout
                    ),
                    actual
                )
            }


        }
    }

    @Test
    fun `when request check is download and there is timeout should return error other`() {
        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.StepDeprecated.DownloadStep
            coEvery { downloadDocService.download() } returns
                    DownloadDocumentService.Response.ErrorResponse(
                        DownloadDocumentService.Error.Conversion(
                            null
                        )
                    )

            launch {
                val actual = collectEmitted<SetupInteractor.Response> {
                    setupInteractor.request(SetupInteractor.Request.Check) {
                        add(it)
                    }
                }
                assertEquals(
                    listOf(
                        SetupInteractor.Response.DownloadStep.Prepare::class.simpleName,
                        SetupInteractor.Response.DownloadStep.Error.Other::class.simpleName
                    ),
                    actual.classesName()
                )

            }
        }
    }


    @Test
    fun `when request check is download and there is ok should download and store document`() {
        runBlocking {
            val document = listOf<Read>(Read.Content.Book(1, "Bookl"))
            every { setupService.getStep() } returns SetupCheckService.StepDeprecated.DownloadStep
            coEvery { downloadDocService.download() } returns DownloadDocumentService.Response.OKResponse(
                document
            )
            launch {
                val actual = collectEmitted<SetupInteractor.Response> {
                    setupInteractor.request(SetupInteractor.Request.Check) {
                        add(it)
                    }
                }
                assertEquals(
                    listOf(
                        SetupInteractor.Response.DownloadStep.Prepare,
                        SetupInteractor.Response.DownloadStep.Persist,
                        SetupInteractor.Response.DownloadStep.Done
                    ),
                    actual
                )
                coVerify { setupService.save(SetupCheckService.StepDeprecated.AuthStep) }
                coVerify { docRepository.save(document) }

            }
        }
    }

}
