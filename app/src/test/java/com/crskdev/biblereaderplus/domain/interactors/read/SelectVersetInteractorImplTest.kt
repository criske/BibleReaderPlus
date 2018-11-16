package com.crskdev.biblereaderplus.domain.interactors.read

import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.entity.SelectedVerset
import com.crskdev.biblereaderplus.domain.entity.VersetProps
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.testutil.classesName
import com.crskdev.biblereaderplus.testutil.collectEmitted
import com.crskdev.biblereaderplus.testutil.RealDispatchersInTestEnvironment
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Created by Cristian Pela on 15.11.2018.
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class SelectVersetInteractorImplTest {

    @MockK
    lateinit var repository: DocumentRepository

    private lateinit var interactor: SelectVersetInteractor

    private val key = Read.Verset.Key(0, 0, 0)

    private val selectedVerset = SelectedVerset(
        key, "Book1", 1, 1, "Foo"
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        interactor = SelectVersetInteractorImpl(
            RealDispatchersInTestEnvironment,
            repository
        )
    }

    @Test
    fun `should return OK and compose the verset with its props`() {
        every { repository.getVerset(any()) } returns selectedVerset
        every { repository.getVersetProps(any()) } returns VersetProps(key, true, emptyList())
        runBlocking {
            val results = collectEmitted<SelectVersetInteractor.Response> {
                interactor.request(key) {
                    add(it)
                }
            }
            assertEquals(listOf("Wait", "OK"), results.classesName())
        }
    }

    @Test
    fun `should return NotFound  error when verset key is not found`() {
        every { repository.getVerset(any()) } returns null
        runBlocking {
            val results = collectEmitted<SelectVersetInteractor.Response> {
                interactor.request(key) {
                    add(it)
                }
            }
            assertEquals(listOf("Wait", "NotFound"), results.classesName())
        }
    }

    @Test
    fun `should return Generic  error when verset props failed`() {
        every { repository.getVerset(any()) } returns selectedVerset
        every { repository.getVersetProps(any()) } throws Exception("Oops!")
        runBlocking {
            val results = collectEmitted<SelectVersetInteractor.Response> {
                interactor.request(key) {
                    add(it)
                }
            }
            assertEquals(listOf("Wait", "Partial", "GenericErr"), results.classesName())
        }
    }
}