/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.interactors.favorite

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Created by Cristian Pela on 20.11.2018.
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class FetchFavoriteVersetsInteractorImplTest {

    /*@Rule
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
    }*/
}