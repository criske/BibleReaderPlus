/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.testutil

import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Cristian Pela on 07.11.2018.
 */
@ExperimentalCoroutinesApi
object TestDispatchers : GatewayDispatchers {

    override val IO: CoroutineDispatcher = Dispatchers.Unconfined

    override val DEFAULT: CoroutineDispatcher = Dispatchers.Unconfined

    override val MAIN: CoroutineDispatcher = Dispatchers.Unconfined

    override fun custom(): CoroutineDispatcher = Dispatchers.Unconfined

}

@ExperimentalCoroutinesApi
object RealDispatchers : GatewayDispatchers {

    override val IO: CoroutineDispatcher = Dispatchers.IO

    override val DEFAULT: CoroutineDispatcher = Dispatchers.Default

    override val MAIN: CoroutineDispatcher = Dispatchers.Main

    override fun custom(): CoroutineDispatcher = Dispatchers.Unconfined

}