/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.setup

import com.crskdev.biblereaderplus.domain.gateway.AuthService
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.DownloadDocumentService
import com.crskdev.biblereaderplus.domain.gateway.SetupCheckService
import com.crskdev.biblereaderplus.testutil.TestDispatchers
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.Before

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


}
