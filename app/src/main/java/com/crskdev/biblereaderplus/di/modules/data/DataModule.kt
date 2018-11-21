/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.data

import com.crskdev.biblereaderplus.di.modules.data.disk.DiskModule
import com.crskdev.biblereaderplus.di.modules.data.io.IOModule
import dagger.Module

/**
 * Created by Cristian Pela on 21.11.2018.
 */
@Module(includes = [DiskModule::class, IOModule::class])
class DataModule {
}