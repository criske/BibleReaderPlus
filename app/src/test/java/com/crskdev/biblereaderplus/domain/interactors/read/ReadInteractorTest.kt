/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.read

import androidx.paging.DataSource
import androidx.paging.PagedList
import com.crskdev.arch.coroutines.paging.dataSourceFactory
import com.crskdev.arch.coroutines.paging.onPaging
import com.crskdev.arch.coroutines.paging.setupPagedListBuilder
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.common.util.pagedlist.dataSourceFactory
import com.crskdev.biblereaderplus.common.util.pagedlist.onPaging
import com.crskdev.biblereaderplus.common.util.pagedlist.setupPagedListBuilder
import com.crskdev.biblereaderplus.common.util.println
import com.crskdev.biblereaderplus.domain.entity.Document
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import com.crskdev.biblereaderplus.testutil.InMemoryPagedListDataSource
import com.crskdev.biblereaderplus.testutil.RealDispatchers
import com.crskdev.biblereaderplus.testutil.TestDispatchers
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

/**
 * Created by Cristian Pela on 08.11.2018.
 */
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class ReadInteractorTest {

    @MockK
    lateinit var docRepository: DocumentRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun request() {
        runBlocking {

            val data = CopyOnWriteArrayList(
                (1..101).fold(mutableListOf<Read>()) { acc, curr ->
                    acc.apply {
                        acc.add(Document.Book("Book$curr", emptyList()))
                    }
                }
            )

            val factory = dataSourceFactory {
                InMemoryPagedListDataSource(data)
            }

            val job = Job()

            val interactor =
                ReadInteractor(TestDispatchers, MockedRepository(RealDispatchers, factory))

            var page: PagedList<Read>? = null

            val displayJob = Job()

            val sendChannel = actor<ReadInteractor.Response>(job) {
                for (response in channel) {
                    response.cast<ReadInteractor.Response.Paged>().list.apply {
                        page = this
                        displayJob.cancelChildren()
                        launch(displayJob) {
                            while (snapshot().isNotEmpty()) {
                                snapshot().map { it.cast<Document.Book>().name }.println()
                                loadAround(lastIndex)
                                delay(1000)
                            }
                        }
                    }
                }
            }


            launch(job) {
                interactor.request(ReadInteractor.Request(sendChannel))
            }

            launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
                delay(6000)
                page?.dataSource?.invalidate()
                delay(7878000)
                job.cancel()
            }

        }
    }
}


class MockedRepository(
    private val dispatchers: GatewayDispatchers,
    private val factory: DataSource.Factory<Int, Read>) : DocumentRepository {

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    override suspend fun read(reader: (PagedList<Read>) -> Unit) = coroutineScope {
        factory
            .setupPagedListBuilder {
                config(3) {
                    enablePlaceholders = false
                }
                fetchDispatcher = dispatchers.DEFAULT
                notifyDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            }
            .onPaging(reader)
    }

    override fun save(document: Document) = TODO()

}