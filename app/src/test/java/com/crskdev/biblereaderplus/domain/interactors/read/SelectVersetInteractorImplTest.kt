package com.crskdev.biblereaderplus.domain.interactors.read

import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.entity.SelectedVerset
import com.crskdev.biblereaderplus.domain.entity.VersetProps
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.remap
import com.crskdev.biblereaderplus.testutil.RealDispatchers
import com.crskdev.biblereaderplus.testutil.TestDispatchers
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import org.junit.Test

import org.junit.Before
import java.util.concurrent.Executors

/**
 * Created by Cristian Pela on 15.11.2018.
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class SelectVersetInteractorImplTest {

    @MockK
    lateinit var repository: DocumentRepository

    lateinit var interactor: SelectVersetInteractor

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        interactor = SelectVersetInteractorImpl(
            RealDispatchers.remap(
                main = Executors.newSingleThreadExecutor { Thread(it, "MAIN") }.asCoroutineDispatcher(),
                default = Executors.newSingleThreadExecutor { Thread(it, "DISK") }.asCoroutineDispatcher(),
                io = Executors.newSingleThreadExecutor { Thread(it, "IO") }.asCoroutineDispatcher()

            ),
            repository
        )
    }

    @Test
    fun request() {

        val key = Read.Verset.Key(0, 0, 0)
        every { repository.getVerset(any()) } returns SelectedVerset(
            key, "Book1", 1, 1, "Foo"
        )
        every { repository.getVersetProps(any()) } returns VersetProps(
            key, true, emptyList()
        )

        runBlocking {
            interactor.request(key) {
               // println("$it")
            }
        }
    }
}