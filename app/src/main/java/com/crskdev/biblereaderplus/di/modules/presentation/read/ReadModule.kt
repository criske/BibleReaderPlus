/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.presentation.read

import com.crskdev.biblereaderplus.di.scopes.PerChildFragment
import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.domain.interactors.read.ContentInteractor
import com.crskdev.biblereaderplus.domain.interactors.read.ReadInteractor
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
    fun provideViewModel(container: ReadFragment, contentInteractor: ContentInteractor): ContentsViewModel =
        viewModelFromProvider(container) {
            ContentsViewModel(contentInteractor)
        }

}

@Module
class PagesModule {

    @PerChildFragment
    @Provides
    fun provideViewModel(container: ReadFragment, readInteractor: ReadInteractor): PagesViewModel =
        viewModelFromProvider(container) {
            PagesViewModel(readInteractor)
        }


}