/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.services

import com.crskdev.biblereaderplus.domain.gateway.SetupCheckService
import com.crskdev.biblereaderplus.domain.gateway.SetupCheckService.Step

/**
 * Created by Cristian Pela on 06.01.2019.
 */
class SetupCheckServiceImpl : SetupCheckService {

    @Volatile
    private var step: Step = Step.UNINITIALIZED

    override fun getStep(): Step = synchronized(this) { step }

    override fun save(step: Step) {
        synchronized(this) {
            this.step = step
        }
    }
}