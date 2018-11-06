/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.gateway

/**
 * Created by Cristian Pela on 06.11.2018.
 */
interface SetupCheckService {

    suspend fun getStep(): Step

    suspend fun next(step: Step): Step

    sealed class Step {
        object None : Step()
        object Uninitialized : Step()
        object DownloadStep : Step()
        object AuthStep : Step()
        object Finished : Step()
        object Initialized : Step()
    }
}