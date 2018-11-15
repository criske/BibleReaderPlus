/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

@file:Suppress("PropertyName")

package com.crskdev.biblereaderplus.domain.gateway

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Created by Cristian Pela on 07.11.2018.
 */
interface GatewayDispatchers {

    val IO: CoroutineDispatcher

    val DEFAULT: CoroutineDispatcher

    val MAIN: CoroutineDispatcher

    fun custom(): CoroutineDispatcher
}

fun GatewayDispatchers.remap(main: CoroutineDispatcher? = null,
                             default: CoroutineDispatcher? = null,
                             io: CoroutineDispatcher? = null): GatewayDispatchers =
    object : GatewayDispatchers {
        override val IO: CoroutineDispatcher = io ?: this@remap.IO
        override val DEFAULT: CoroutineDispatcher = default ?: this@remap.DEFAULT
        override val MAIN: CoroutineDispatcher = main ?: this@remap.MAIN
        override fun custom(): CoroutineDispatcher = this@remap.custom()
    }