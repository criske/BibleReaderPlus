/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.setup

import com.crskdev.biblereaderplus.domain.entity.DeviceAccountCredential
import com.crskdev.biblereaderplus.domain.gateway.AuthService
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.DownloadDocumentService
import com.crskdev.biblereaderplus.domain.gateway.SetupCheckService
import com.crskdev.biblereaderplus.testutil.TestDispatchers
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
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
        setupInteractor = SetupInteractor(
            TestDispatchers,
            setupService,
            authService,
            downloadDocService,
            docRepository
        )
    }

    @Test
    fun `when request check should return none`() {

        runBlocking {

            every { setupService.getStep() } returns SetupCheckService.Step.Uninitialized

            val responseChannel = Channel<SetupInteractor.Response>()
            launch {
                setupInteractor.request(SetupInteractor.Request.Check(responseChannel))
            }

            assertEquals(
                listOf(SetupInteractor.Response.DownloadStep.Prepare),
                responseChannel.toCollection(mutableListOf())
            )

            verify { setupService.next(SetupCheckService.Step.DownloadStep) }
        }
    }

    @Test
    fun `when on auth step and no permission should return need permission then provide credentials`() {

        runBlocking {
            every { setupService.getStep() } returns SetupCheckService.Step.AuthStep
            every { authService.hasPermission() } returns false
            coEvery { authService.authenticate(any()) } returns Pair<Error?, Boolean>(null, true)
            coEvery { authService.authenticateWithPermissionGranted() } returns Pair<Error?, Boolean>(
                null,
                true
            )

            var responseChannel = Channel<SetupInteractor.Response>()
            launch {
                setupInteractor.request(SetupInteractor.Request.Check(responseChannel))
            }
            assertEquals(
                listOf(
                    SetupInteractor.Response.AuthStep.Prepare,
                    SetupInteractor.Response.AuthStep.NeedPermission
                ),
                responseChannel.toCollection(mutableListOf())
            )
            verify { authService.requestPermission() }

            responseChannel = Channel()
            launch {
                setupInteractor.request(
                    SetupInteractor.Request.AuthPrompt(
                        DeviceAccountCredential.AuthorizationPayload(
                            Unit
                        ), responseChannel
                    )
                )
            }
            assertEquals(
                listOf(
                    SetupInteractor.Response.AuthStep.Prepare,
                    SetupInteractor.Response.AuthStep.Authenticating,
                    SetupInteractor.Response.AuthStep.Done,
                    SetupInteractor.Response.Finished
                ),
                responseChannel.toCollection(mutableListOf())
            )


        }

    }
}
