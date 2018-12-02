/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.favorite

import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.RemoteDocumentRepository
import com.crskdev.biblereaderplus.testutil.TestDispatchers
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Created by Cristian Pela on 18.11.2018.
 */
class FavoriteActionsVersetInteractorImplTest {

    @MockK
    lateinit var localRepository: DocumentRepository
    @MockK
    lateinit var remoteRepository: RemoteDocumentRepository

    private lateinit var interactor: FavoriteActionsVersetInteractor

    private val versetKey = VersetKey(0, 0, 0, "remote-key")

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        interactor = FavoriteActionsVersetInteractorImpl(
            TestDispatchers,
            localRepository,
            remoteRepository
        )
    }

    @Test
    fun `should call local and remote favorite action functions`() {
        runBlocking {
            interactor.request(versetKey, true)
            verify { localRepository.favoriteAction(versetKey, true) }
            verify { remoteRepository.favoriteAction(versetKey, true) }
        }
    }


}