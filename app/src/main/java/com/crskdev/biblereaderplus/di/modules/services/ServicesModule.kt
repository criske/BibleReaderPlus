/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.di.modules.services

import com.crskdev.biblereaderplus.domain.gateway.SetupCheckService
import com.crskdev.biblereaderplus.services.SetupCheckServiceImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Cristian Pela on 06.01.2019.
 */
@Module
class ServicesModule {

    @Provides
    @Singleton
    fun provideSetupCheckService(): SetupCheckService = SetupCheckServiceImpl()

}