/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.services

import com.crskdev.biblereaderplus.domain.gateway.SetupCheckService

/**
 * Created by Cristian Pela on 06.01.2019.
 */
class SetupCheckServiceImpl : SetupCheckService {

    @Volatile
    private var step: SetupCheckService.Step = SetupCheckService.Step.Uninitialized

    override fun getStep(): SetupCheckService.Step = step

    override fun next(step: SetupCheckService.Step) {
        synchronized(this) {
            this.step = step
        }
    }
}