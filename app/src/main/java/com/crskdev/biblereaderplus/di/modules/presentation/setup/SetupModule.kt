/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.di.modules.presentation.setup

import com.crskdev.biblereaderplus.di.scopes.PerChildFragment
import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.domain.interactors.setup.SetupInteractor
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

    @PerChildFragment
    @ContributesAndroidInjector(modules = [DownloadStepModule::class])
    abstract fun downloadFragmentInjector(): DownloadStepFragment

    @PerChildFragment
    @ContributesAndroidInjector(modules = [AuthStepModule::class])
    abstract fun authFragmentInjector(): AuthStepFragment

    @PerChildFragment
    @ContributesAndroidInjector(modules = [FinishedModule::class])
    abstract fun finishedFragmentInjector(): FinishedStepFragment

    @Module
    companion object {
        @JvmStatic
        @Provides
        @PerFragment
        fun provideViewModel(container: SetupFragment,
                             setupInteractor: SetupInteractor): SetupViewModel =
            viewModelFromProvider(container) {
                SetupViewModel(setupInteractor)
            }
    }

}

@Module
class DownloadStepModule {
    @Provides
    @PerChildFragment
    fun provideViewModel(container: SetupFragment): DownloadStepViewModel =
        viewModelFromProvider(container) {
            DownloadStepViewModel()
        }
}

@Module
class AuthStepModule {
    @Provides
    @PerChildFragment
    fun provideViewModel(container: SetupFragment): AuthStepViewModel =
        viewModelFromProvider(container) {
            AuthStepViewModel()
        }

}

@Module
class FinishedModule {
    @Provides
    @PerChildFragment
    fun provideViewModel(container: SetupFragment): FinishedStepViewModel =
        viewModelFromProvider(container) {
            FinishedStepViewModel()
        }

}