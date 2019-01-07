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

    override fun getStep(): SetupCheckService.Step =
        SetupCheckService.Step.Initialized

    override fun next(step: SetupCheckService.Step) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}