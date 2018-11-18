/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

@file:Suppress("PropertyName")

package com.crskdev.biblereaderplus.domain.gateway

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Created by Cristian Pela on 07.11.2018.
 */
interface GatewayDispatchers {

    val IO: CoroutineDispatcher

    val DEFAULT: CoroutineDispatcher

    val MAIN: CoroutineDispatcher

    val UNCONFINED: CoroutineDispatcher

    fun custom(): CoroutineDispatcher
}

fun GatewayDispatchers.remap(main: CoroutineDispatcher? = null,
                             default: CoroutineDispatcher? = null,
                             io: CoroutineDispatcher? = null,
                             unconfined: CoroutineDispatcher? = null): GatewayDispatchers =
    object : GatewayDispatchers {
        override val IO: CoroutineDispatcher = io ?: this@remap.IO
        override val DEFAULT: CoroutineDispatcher = default ?: this@remap.DEFAULT
        override val MAIN: CoroutineDispatcher = main ?: this@remap.MAIN
        override val UNCONFINED: CoroutineDispatcher = unconfined ?: this@remap.UNCONFINED
        override fun custom(): CoroutineDispatcher = this@remap.custom()
    }