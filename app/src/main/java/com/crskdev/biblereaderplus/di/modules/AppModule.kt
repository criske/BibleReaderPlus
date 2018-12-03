/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules

import android.content.Context
import com.crskdev.biblereaderplus.BibleReaderApplication
import com.crskdev.biblereaderplus.di.modules.common.CommonModule
import com.crskdev.biblereaderplus.di.modules.data.DataModule
import com.crskdev.biblereaderplus.di.modules.domain.interactors.InteractorsModule
import com.crskdev.biblereaderplus.di.modules.presentation.SingleActivityModule
import com.crskdev.biblereaderplus.di.scopes.PerActivity
import com.crskdev.biblereaderplus.presentation.MainActivity
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi


/**
 * Created by Cristian Pela on 05.11.2018.
 */
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@Module(
    includes = [
        AndroidInjectionModule::class,
        CommonModule::class,
        DataModule::class,
        InteractorsModule::class
    ]
)
abstract class AppModule {

    @Binds
    abstract fun bindContext(application: BibleReaderApplication): Context

    @PerActivity
    @ContributesAndroidInjector(modules = [SingleActivityModule::class])
    abstract fun mainActivityInjector(): MainActivity

}