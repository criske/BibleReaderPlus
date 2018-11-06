/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.setup

import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.DownloadDocumentService
import com.crskdev.biblereaderplus.domain.gateway.SetupCheckService
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.take
import kotlinx.coroutines.channels.toCollection
import kotlinx.coroutines.coroutineScope
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

    @Before
    fun setup() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun `when request check should return none`() {

        runBlocking {

            val setupInteractor =
                SetupInteractor(MockSetupCheckService(), downloadDocService, docRepository)

            val responses = setupInteractor
                .request(SetupInteractor.Request.Check)
                .take(1)
                .toCollection(mutableListOf())

            assertEquals(
                listOf(
                    SetupInteractor.Response.DownloadStep.Prepare(100)
                ), responses
            )
        }


    }
}

class MockSetupCheckService : SetupCheckService {

    var currentStep: SetupCheckService.Step = SetupCheckService.Step.None

    override suspend fun getStep(): SetupCheckService.Step = coroutineScope {
        currentStep
    }

    override suspend fun next(step: SetupCheckService.Step): SetupCheckService.Step =
        coroutineScope {
            currentStep = step
            currentStep

        }


}