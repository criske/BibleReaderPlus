/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.presentation.read

import com.crskdev.biblereaderplus.di.scopes.PerChildFragment
import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.presentation.read.ContentsFragment
import com.crskdev.biblereaderplus.presentation.read.PagesFragment
import com.crskdev.biblereaderplus.presentation.read.ReadFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


/**
 * Created by Cristian Pela on 05.11.2018.
 */
@Module
abstract class ReadModule {

    @PerFragment
    @ContributesAndroidInjector(modules = [ContentsModule::class, PagesModule::class])
    internal abstract fun readFragmentInjector(): ReadFragment

}

@Module
abstract class ContentsModule {

    @PerChildFragment
    @ContributesAndroidInjector(modules = [])
    internal abstract fun contentsFragmentInjector(): ContentsFragment

}

@Module
abstract class PagesModule {

    @PerChildFragment
    @ContributesAndroidInjector(modules = [])
    internal abstract fun readFragmentInjector(): PagesFragment

}