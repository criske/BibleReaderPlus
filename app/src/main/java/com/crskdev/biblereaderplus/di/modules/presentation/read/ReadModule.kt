/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.presentation.read

import com.crskdev.biblereaderplus.di.scopes.PerChildFragment
import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.presentation.read.*
import com.crskdev.biblereaderplus.presentation.util.arch.viewModelFromProvider
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector


/**
 * Created by Cristian Pela on 05.11.2018.
 */
@Module
abstract class ReadModule {

    @PerChildFragment
    @ContributesAndroidInjector(modules = [ContentsModule::class])
    abstract fun contentsFragmentInjector(): ContentsFragment

    @PerChildFragment
    @ContributesAndroidInjector(modules = [PagesModule::class])
    abstract fun pagesFragmentInjector(): PagesFragment

    @Module
    companion object {
        @JvmStatic
        @PerFragment
        @Provides
        fun provideViewModel(container: ReadFragment): ReadViewModel =
            viewModelFromProvider(container) {
                ReadViewModel()
            }

    }


}

@Module
class ContentsModule {

    @PerChildFragment
    @Provides
    fun provideViewModel(container: ReadFragment): ContentsViewModel =
        viewModelFromProvider(container) {
            ContentsViewModel()
        }

}

@Module
class PagesModule {

    @PerChildFragment
    @Provides
    fun provideViewModel(container: ReadFragment): PagesViewModel =
        viewModelFromProvider(container) {
            PagesViewModel()
        }


}