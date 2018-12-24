/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.common.util

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.select
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by Cristian Pela on 08.11.2018.
 */
suspend fun <T> retry(
    times: Int = Int.MAX_VALUE,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    tracker: (Int, Throwable) -> Unit = { _, _ -> },
    block: suspend () -> T): T {
    var currentDelay = initialDelay
    for (i in 1..times) {
        try {
            return block()
        } catch (e: java.lang.Exception) {
            tracker(i, e)
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}

suspend fun <T> retryWhen(
    retryWhen: (Int, T) -> Boolean,
    times: Int = Int.MAX_VALUE,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> T): T {
    var currentDelay = initialDelay
    for (i in 1..times) {
        val result = block()
        if (!retryWhen(i, result)) {
            return result
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}


suspend fun <T> Deferred<T>.awaitOn(coroutineContext: CoroutineContext): T =
    withContext(coroutineContext) {
        this@awaitOn.await()
    }

suspend fun <T> SendChannel<T>.sendAndClose(element: T, throwable: Throwable? = null) {
    with(this) {
        send(element)
        close(throwable)
    }
}


suspend fun CoroutineScope.launchIgnoreThrow(context: CoroutineContext = EmptyCoroutineContext,
                                             handler: suspend (CoroutineContext, Throwable) -> Unit = { _, _ -> },
                                             block: suspend CoroutineScope.() -> Unit) =
    supervisorScope {
        val ctxWithExceptionHandling =
            coroutineContext + context + CoroutineExceptionHandler { ctx, err ->
                launch {
                    handler(ctx, err)
                }
            }
        launch(ctxWithExceptionHandling) {
            this.block()
        }
    }

fun CoroutineScope.withDispatcher(dispatcher: CoroutineDispatcher): CoroutineContext =
    coroutineContext + dispatcher

suspend fun <T> CoroutineScope.switchSelectOnReceive(receiveChannel: ReceiveChannel<T>,
                                                     selectBlock: suspend (Job, T) -> Unit) {
    var switchJob = Job()
    while (true) {
        select<Unit> {
            receiveChannel.onReceive {
                switchJob.cancel()
                switchJob = Job()
                selectBlock(switchJob, it)
            }
        }
    }
}

suspend fun <T> ChannelWith(default: T? = null) = Channel<T?>(Channel.CONFLATED).apply {
    send(default)
}
