/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.read

import androidx.paging.PagedList
import androidx.paging.PositionalDataSource
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.common.util.println
import com.crskdev.biblereaderplus.domain.entity.Document
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.testutil.RealDispatchers
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
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

    @Test
    fun request() {
        runBlocking {

            val data = listOf(
                Document.Book("Foo", emptyList()),
                Document.Book("Bar", emptyList()),
                Document.Book("War", emptyList()),
                Document.Book("Far", emptyList()),
                Document.Book("Dar", emptyList()),
                Document.Book("Qar", emptyList())
            )

            coEvery { docRepository.read(any()) } coAnswers {
                coroutineScope {


                    val pagingJob = Job()
                    val sendChannel = arg<SendChannel<PagedList<Read>>>(0)

                    val ds = object : PositionalDataSource<Read>() {
                        override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Read>) {
                            val endPosition =
                                (params.startPosition + params.loadSize).coerceAtMost(data.size)
                            val subList = data.subList(params.startPosition, endPosition)
                            callback.onResult(subList)
                        }

                        override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Read>) {
                            val totalCount = data.size
                            val firstLoadPosition =
                                PositionalDataSource.computeInitialLoadPosition(
                                    params,
                                    totalCount
                                )
                            val firstLoadSize =
                                PositionalDataSource.computeInitialLoadSize(
                                    params,
                                    firstLoadPosition,
                                    totalCount
                                )
                            callback.onResult(
                                data.subList(firstLoadPosition, firstLoadSize),
                                firstLoadPosition,
                                firstLoadSize
                            )
                        }
                    }

                    val builder = PagedList
                        .Builder(
                            ds, PagedList.Config.Builder()
                                .setPageSize(1)
                                .setEnablePlaceholders(false)
                                .build()
                        )
                        .setNotifyExecutor {
                            //launch(Dispatchers.IO + pagingJob) {
                            it.run()
                            //}
                        }
                        .setFetchExecutor {
                            // launch(Dispatchers.Default + pagingJob) {
                            it.run()
                            // }
                        }


                    var lastPage: PagedList<Read>? = null
                    ds.addInvalidatedCallback {
                        launch(pagingJob) {
                            val lastKey = lastPage?.lastKey?.cast<Int>()
                            sendChannel.send(builder.setInitialKey(lastKey).build().apply {
                                lastPage = this
                            })
                        }
                    }

                    sendChannel.send(builder.setInitialKey(null).build().apply {
                        lastPage = this
                    })

                    sendChannel.invokeOnClose {
                        pagingJob.cancel()
                    }
                }
            }

            //note if  I'm using the test dispatchers it hangs
            val interactor = ReadInteractor(RealDispatchers, docRepository)

            val job = Job()

            val sendChannel = actor<ReadInteractor.Response>(job) {
                //                val receive = channel.receive()
//                assertTrue(receive is ReadInteractor.Response.Paged)
//                val page = receive.cast<ReadInteractor.Response.Paged>()
//                page.list.snapshot().apply {
//                    println(this.map { it.cast<Document.Book>().name })
//                    assertEquals(3, size)
//                    assertTrue(first() is Document.Book)
//                    assertEquals("Foo", first().cast<Document.Book>().name)
//                }
//                page.list.dataSource.invalidate()
//                page.list.snapshot().apply {
//                    println(this.map { it.cast<Document.Book>().name })
////                    assertEquals(6, size)
////                    assertTrue(first() is Document.Book)
////                    assertEquals("Qar", last().cast<Document.Book>().name)
//                }

                channel.receive().cast<ReadInteractor.Response.Paged>().list.apply {
                    Thread.currentThread().println()

                    map { it.cast<Document.Book>().name }.println()
                    loadAround(lastIndex)
                    map { it.cast<Document.Book>().name }.println()
                    loadAround(lastIndex)
                    map { it.cast<Document.Book>().name }.println()
                    loadAround(lastIndex)
                    map { it.cast<Document.Book>().name }.println()
                    loadAround(lastIndex)
                    map { it.cast<Document.Book>().name }.println()
                }

            }
            launch {
                delay(1000)
                job.cancel()
            }

            launch(job) {
                interactor.request(ReadInteractor.Request(sendChannel))
            }

        }
    }
}