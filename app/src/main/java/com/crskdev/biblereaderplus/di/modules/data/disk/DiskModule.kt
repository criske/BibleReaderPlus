/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.di.modules.data.disk

import android.content.Context
import com.crskdev.biblereaderplus.data.DocumentRepositoryImpl
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Cristian Pela on 21.11.2018.
 */
@Module
class DiskModule {

    @Provides
    @Singleton
    fun provideRepository(context: Context): DocumentRepository {
        //return RoomDocumentRepositoryImpl(context)
        return DocumentRepositoryImpl()
    }

}