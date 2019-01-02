/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.read

import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

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

            //            val data = CopyOnWriteArrayList(
//                (1..101).fold(mutableListOf<Read>()) { acc, curr ->
//                    acc.apply {
//                        acc.add(Document.Book("Book$curr", emptyList()))
//                    }
//                }
//            )
//
//            val factory = dataSourceFactory {
//                InMemoryPagedListDataSource(data)
//            }
//
//            val job = Job()
//
//            val interactor =
//                ReadInteractor(TestDispatchers, MockedRepository(RealDispatchers, factory))
//
//            var page: PagedList<Read>? = null
//
//            val displayJob = Job()
//
//            val sendChannel = actor<ReadInteractor.Response>(job) {
//                for (response in channel) {
//                    response.cast<ReadInteractor.Response.Paged>().list.apply {
//                        page = this
//                        displayJob.cancelChildren()
//                        launch(displayJob) {
//                            while (snapshot().isNotEmpty()) {
//                                snapshot().map { it.cast<Document.Book>().name }.println()
//                                loadAround(lastIndex)
//                                delay(1000)
//                            }
//                        }
//                    }
//                }
//            }
//
//
//            launch(job) {
//                interactor.request(ReadInteractor.Request(sendChannel))
//            }
//
//            launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
//                delay(6000)
//                page?.dataSource?.invalidate()
//                delay(7878000)
//                job.cancel()
//            }
//
        }
    }
}


//class MockedRepository(
//    private val dispatchers: GatewayDispatchers,
//    private val factory: DataSource.Factory<Int, Read>) : DocumentRepository {
//
//    @ObsoleteCoroutinesApi
//    @ExperimentalCoroutinesApi
//    override suspend fun read(reader: (PagedList<Read>) -> Unit) = coroutineScope {
//        factory
//            .setupPagedListBuilder {
//                config(3) {
//                    enablePlaceholders = false
//                }
//                fetchDispatcher = dispatchers.DEFAULT
//                notifyDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
//            }
//            .onPaging(reader)
//    }
//
//    override fun save(document: Document) = TODO()
//
//}