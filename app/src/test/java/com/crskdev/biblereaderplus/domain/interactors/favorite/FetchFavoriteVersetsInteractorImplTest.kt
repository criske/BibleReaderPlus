package com.crskdev.biblereaderplus.domain.interactors.favorite

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.crskdev.arch.coroutines.paging.dataSourceFactory
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
            every { repository.favorites() } returns
                    (1..100).map {
                        Read.Verset(VersetKey(it, 1, 1), it, "", false, ModifiedAt(""))
                    }.let { l -> dataSourceFactory { InMemoryPagedListDataSource(l) } }

            val filterChannel = Channel<FavoriteFilter>()
            val mainJob = Job()
            launch(mainJob) {
                interactor.request(filterChannel) {
                    println("page: ${it.snapshot().map { v -> "<${v?.key?.id} ${v?.key?.bookId} ${v?.key?.chapterId}>" }}")
                    it.loadAround(it.snapshot().lastIndex)
                    println("page: ${it.map { v -> "<${v?.key?.id} ${v?.key?.bookId} ${v?.key?.chapterId}>" }}")
                }
            }
            launch {
                listOf(
                    FavoriteFilter.None,
                    FavoriteFilter.ByLastModified.ASC,
                    FavoriteFilter.ByLastModified.DESC
                ).forEach {
                    filterChannel.send(it)
                    delay(2000)
                }
                mainJob.cancel()
            }
            Unit

        }
    }
}