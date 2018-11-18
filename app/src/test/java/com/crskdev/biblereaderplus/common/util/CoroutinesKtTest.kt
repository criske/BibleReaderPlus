package com.crskdev.biblereaderplus.common.util

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import org.junit.Test
import kotlin.coroutines.CoroutineContext

/**
 * Created by Cristian Pela on 16.11.2018.
 */
@ExperimentalCoroutinesApi
class CoroutinesKtTest {

    private val func = mockk<() -> Int>(relaxed = true)

    @Test
    fun `should retry when error thrown`() {
        every { func() } throws Exception("Oops!")
        runBlocking {
            coroutineScope {
                val sender = actor<Int> {
                    for (r in channel) {
                        println("Response : $r")
                    }
                }

                sender.send(0)

                val handler: suspend (CoroutineContext, Throwable) -> Unit = { _, e ->
                    sender.sendAndClose(-10000)
                }

                launchIgnoreThrow(handler = handler) {
                    val a: Int = withContext(coroutineContext + Dispatchers.Default) {
                        1
                    }
                    val b = retry(3, tracker = { i, ex -> if(i == 1) {
                            launch {
                                sender.send(a)
                            }
                        }
                    }) {
                        withContext(coroutineContext + Dispatchers.IO) {
                            func()
                        }
                    }

                    sender.sendAndClose(a + b)
                }
                Unit
            }

            //3 retries and 1 last call before give up
            //verify(exactly = 3 + 1) { func() }
        }
    }


    @Test
    fun `should not retry when call succeedes`() {
        runBlocking {
            retry(3) {
                func()
            }
            verify(exactly = 1) { func() }
        }
    }

}


