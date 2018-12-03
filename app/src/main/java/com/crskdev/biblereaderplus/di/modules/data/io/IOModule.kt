/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.data.io

import com.crskdev.biblereaderplus.data.RemoteDocumentRepositoryImpl
import com.crskdev.biblereaderplus.domain.gateway.RemoteDocumentRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Cristian Pela on 21.11.2018.
 */
@Module
class IOModule {

    @Provides
    @Singleton
    fun provideRemoteRepository(): RemoteDocumentRepository = RemoteDocumentRepositoryImpl()

}