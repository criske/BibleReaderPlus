/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.common.util

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by Cristian Pela on 08.11.2018.
 */
suspend fun <T> retry(
    times: Int = Int.MAX_VALUE,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> T): T = coroutineScope {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            withContext(coroutineContext) { block() }
        } catch (e: Exception) {
            // you can log an error here and/or make a more finer-grained
            // analysis of the cause to see if retry is needed
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    withContext(coroutineContext) { block() } // last attempt

}

suspend fun <T> Deferred<T>.awaitOn(coroutineContext: CoroutineContext): T =
    withContext(coroutineContext) {
        this@awaitOn.await()
    }
