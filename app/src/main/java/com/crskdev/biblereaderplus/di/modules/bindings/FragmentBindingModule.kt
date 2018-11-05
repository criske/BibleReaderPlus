/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.bindings

import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.presentation.read.ReadFragment
import com.crskdev.biblereaderplus.presentation.setup.SetupFragment
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector

/**
 * Created by Cristian Pela on 05.11.2018.
 */

@Module(includes = [AndroidInjectionModule::class])
abstract class FragmentBindingModule {

    @PerFragment
    @ContributesAndroidInjector
    abstract fun setupFragmentInjector(): SetupFragment

    @PerFragment
    @ContributesAndroidInjector
    abstract fun readFragmentInjector(): ReadFragment

}