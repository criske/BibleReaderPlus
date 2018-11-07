/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.gateway

/**
 * Created by Cristian Pela on 06.11.2018.
 */
interface SetupCheckService {

    fun getStep(): Step

    fun next(step: Step)

    sealed class Step {
        object Initialized : Step()
        object Uninitialized : Step()
        object DownloadStep : Step()
        object AuthStep : Step()
        object Finished : Step()
        class Error(val err: Throwable) : Step()
    }
}