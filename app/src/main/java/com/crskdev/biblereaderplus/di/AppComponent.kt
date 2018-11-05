/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di

import com.crskdev.biblereaderplus.BibleReaderApplication
import com.crskdev.biblereaderplus.di.modules.AppModule
import com.crskdev.biblereaderplus.di.modules.bindings.ActivityBindingsModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


/**
 * Created by Cristian Pela on 05.11.2018.
 */
@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, ActivityBindingsModule::class, AppModule::class])
interface AppComponent : AndroidInjector<BibleReaderApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: BibleReaderApplication): AppComponent.Builder

        fun build(): AppComponent
    }
}