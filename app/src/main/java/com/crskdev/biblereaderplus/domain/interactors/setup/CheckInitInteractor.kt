/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.interactors.setup

import com.crskdev.biblereaderplus.domain.gateway.SetupCheckService

/**
 * Created by Cristian Pela on 07.01.2019.
 */
interface CheckInitInteractor {
    fun requestIsInitialized(): Boolean
}

class CheckInitInteractorImpl(private val setupCheckService: SetupCheckService) :
    CheckInitInteractor {

    override fun requestIsInitialized(): Boolean = setupCheckService.getStep() ==
            SetupCheckService.Step.Initialized

}