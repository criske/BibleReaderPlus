/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.presentation.setup

import com.crskdev.biblereaderplus.di.scopes.PerChildFragment
import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.presentation.setup.*
import com.crskdev.biblereaderplus.presentation.util.arch.viewModelFromProvider
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

/**
 * Created by Cristian Pela on 05.11.2018.
 */
@Module
abstract class SetupModule {

    @PerFragment
    @ContributesAndroidInjector(
        modules = [
            DownloadStepModule::class,
            AuthStepModule::class,
            FinishedModule::class
        ]
    )
    abstract fun setupFragmentInjector(): SetupFragment

    @Provides
    @PerFragment
    fun provideViewModel(container: SetupFragment): SetupViewModel =
        viewModelFromProvider(container) {
            SetupViewModel()
        }
}

@Module
abstract class DownloadStepModule {

    @PerChildFragment
    @ContributesAndroidInjector
    abstract fun downloadFragmentInjector(): DownloadStepFragment

}

@Module
abstract class AuthStepModule {

    @PerChildFragment
    @ContributesAndroidInjector
    abstract fun authFragmentInjector(): AuthStepFragment

}

@Module
abstract class FinishedModule {

    @PerChildFragment
    @ContributesAndroidInjector
    abstract fun finishedFragmentInjector(): FinishedStepFragment

}