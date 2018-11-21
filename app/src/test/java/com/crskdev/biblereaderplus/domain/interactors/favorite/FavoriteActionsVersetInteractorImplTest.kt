/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.favorite

import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.testutil.TestDispatchers
import com.crskdev.biblereaderplus.testutil.classesName
import com.crskdev.biblereaderplus.testutil.collectEmitted
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Created by Cristian Pela on 18.11.2018.
 */
class FavoriteActionsVersetInteractorImplTest {

    @MockK
    lateinit var repository: DocumentRepository

    private lateinit var interactor: FavoriteActionsVersetInteractor

    private val versetKey = VersetKey(0, 0, 0)

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        interactor = FavoriteActionsVersetInteractorImpl(TestDispatchers, repository)
    }

    @Test
    fun `should response with error when not add or remove favorite`() {
        every { repository.favoriteAction(any(), any()) } throws Error("Oops")
        runBlocking {
            val actual = collectEmitted<FavoriteActionsVersetInteractor.Response> {
                interactor.request(versetKey, true) {
                    add(it)
                }
            }.classesName()
            assertEquals(listOf("Wait", "Error"), actual)
        }
    }

    @Test
    fun `should response with ok when add to favorites`() {
        runBlocking {
            val actual = collectEmitted<FavoriteActionsVersetInteractor.Response> {
                interactor.request(versetKey, true) {
                    add(it)
                }
            }.classesName()
            assertEquals(listOf("Wait", "OK"), actual)

            verify { repository.favoriteAction(versetKey, true) }
        }
    }

    @Test
    fun `should response with ok when removed from favorites`() {
        runBlocking {
            val actual = collectEmitted<FavoriteActionsVersetInteractor.Response> {
                interactor.request(versetKey, false) {
                    add(it)
                }
            }.classesName()
            assertEquals(listOf("Wait", "OK"), actual)

            verify { repository.favoriteAction(versetKey, false) }
        }
    }
}