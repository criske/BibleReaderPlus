/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.bindings

import com.crskdev.biblereaderplus.di.scopes.PerActivity
import com.crskdev.biblereaderplus.presentation.MainActivity
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector


/**
 * Created by Cristian Pela on 05.11.2018.
 */
@Module(includes = [AndroidInjectionModule::class, FragmentBindingModule::class])
abstract class ActivityBindingsModule {

    @PerActivity
    @ContributesAndroidInjector()
    abstract fun mainActivityInjector(): MainActivity

}