/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.presentation

import com.crskdev.biblereaderplus.di.modules.presentation.read.ReadModule
import com.crskdev.biblereaderplus.di.modules.presentation.setup.SetupModule
import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.presentation.read.ReadFragment
import com.crskdev.biblereaderplus.presentation.setup.SetupFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Cristian Pela on 05.11.2018.
 */

@Module
abstract class MainActivityModule {

    @PerFragment
    @ContributesAndroidInjector(modules = [SetupModule::class])
    abstract fun setupFragmentInjector(): SetupFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [ReadModule::class])
    abstract fun readFragmentInjector(): ReadFragment

}



