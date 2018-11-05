/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.presentation

import com.crskdev.biblereaderplus.di.modules.presentation.read.ReadModule
import com.crskdev.biblereaderplus.di.modules.presentation.setup.SetupModule
import com.crskdev.biblereaderplus.di.scopes.PerActivity
import com.crskdev.biblereaderplus.presentation.MainActivity
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector

/**
 * Created by Cristian Pela on 05.11.2018.
 */

@Module(includes = [AndroidInjectionModule::class])
abstract class MainActivityModule {

    @PerActivity
    @ContributesAndroidInjector(modules = [SetupModule::class, ReadModule::class])
    abstract fun mainActivityInjector(): MainActivity

}