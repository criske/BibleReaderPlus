/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.presentation.setup

import com.crskdev.biblereaderplus.di.scopes.PerChildFragment
import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.presentation.setup.AuthStepFragment
import com.crskdev.biblereaderplus.presentation.setup.DownloadStepFragment
import com.crskdev.biblereaderplus.presentation.setup.FinishedStepFragment
import com.crskdev.biblereaderplus.presentation.setup.SetupFragment
import dagger.Module
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