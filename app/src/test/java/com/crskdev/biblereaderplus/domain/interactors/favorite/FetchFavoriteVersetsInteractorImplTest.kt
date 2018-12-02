/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.favorite

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.crskdev.arch.coroutines.paging.dataSourceFactory
import com.crskdev.biblereaderplus.domain.entity.FavoriteFilter
import com.crskdev.biblereaderplus.domain.entity.ModifiedAt
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.testutil.InMemoryPagedListDataSource
import com.crskdev.biblereaderplus.testutil.TestDispatchers
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Created by Cristian Pela on 20.11.2018.
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class FetchFavoriteVersetsInteractorImplTest {

    @Rule
    @JvmField
    val instantTaskTestRule = InstantTaskExecutorRule()

    @MockK
    lateinit var repository: DocumentRepository

    private lateinit var interactor: FetchFavoriteVersetsInteractor

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        interactor = FetchFavoriteVersetsInteractorImpl(TestDispatchers, repository)
    }

    @Test
    fun `should change paging when change filter`() {
        runBlocking {
            every { repository.favorites(any()) } answers {
                val filter = firstArg<FavoriteFilter>()
                (1..100)
                    .map { id ->
                        Read.Verset(
                            VersetKey(id, 1, 1, "id"),
                            id,
                            filter.toString(),
                            false,
                            ModifiedAt("")
                        )
                    }
                    .let { l ->
                        dataSourceFactory { InMemoryPagedListDataSource(l) }
                    }
            }
            val filters = listOf(FavoriteFilter.NONE)
            val filterChannel = Channel<FavoriteFilter>()
            val mainJob = Job().apply {
                invokeOnCompletion {
                    filterChannel.close(it)
                }
            }

            launch(mainJob) {
                //pick the filter in content of each paged list batch, then compare
                interactor.request(filterChannel) {
                    assertEquals(FavoriteFilter.NONE.toString(), it.first().content)
                }
            }
            launch{
                filters.forEach {
                    filterChannel.send(it)
                    delay(500)
                }
                mainJob.cancel()
            }
            Unit

        }
    }
}