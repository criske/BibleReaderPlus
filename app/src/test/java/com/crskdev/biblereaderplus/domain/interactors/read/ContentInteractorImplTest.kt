/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.read

import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.testutil.TestDispatchers
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Created by Cristian Pela on 14.11.2018.
 */
@ExperimentalCoroutinesApi
class ContentInteractorImplTest {

    @MockK
    lateinit var repository: DocumentRepository

    lateinit var contentInteractor: ContentInteractor

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        contentInteractor = ContentInteractorImpl(TestDispatchers, repository)
    }

    @Test
    fun `should return all the contents when query is null`() {
        every { repository.contents() } returns emptyList()
        runBlocking {
            val actualContents = contentInteractor.request("   ")
            assertTrue(actualContents is ContentInteractor.Response.OK)
            verify { repository.contents() }
        }
    }

    @Test
    fun `should return all the contents when query is empty`() {
        every { repository.contents() } returns emptyList()
        runBlocking {
            val actualContents = contentInteractor.request("   ")
            assertTrue(actualContents is ContentInteractor.Response.OK)
            verify { repository.contents() }
        }
    }

    @Test
    fun `should error when query is not empty and length is less than 3`() {
        runBlocking {
            val actualContents = contentInteractor.request("a")
            assertTrue(actualContents is ContentInteractor.Response.Error.InvalidLength)
        }
    }

    @Test
    fun `should filter when query is not empty and length is at least larger than 3`() {
        every { repository.filterContents(any()) } returns emptyList()
        runBlocking {
            contentInteractor.request("aaa")
            verify { repository.filterContents(any()) }
        }
    }
}