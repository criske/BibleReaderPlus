/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules

import android.app.Application
import android.content.Context
import com.crskdev.biblereaderplus.di.scopes.PerActivity
import com.crskdev.biblereaderplus.presentation.MainActivity
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector


/**
 * Created by Cristian Pela on 05.11.2018.
 */
@Module(includes = [AndroidInjectionModule::class])
abstract class AppModule {

    @Binds
    abstract fun bindContext(application: Application): Context

    @PerActivity
    @ContributesAndroidInjector()
    abstract fun mainActivityInjector(): MainActivity

}