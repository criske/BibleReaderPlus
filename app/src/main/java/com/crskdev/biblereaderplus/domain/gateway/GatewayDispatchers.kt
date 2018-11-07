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