/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module


/**
 * Created by Cristian Pela on 05.11.2018.
 */
@Module
abstract class AppModule {

    @Binds
    abstract fun bindContext(application: Application): Context

}