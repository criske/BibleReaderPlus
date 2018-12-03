/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.presentation

import android.content.Context
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.enumMap
import com.crskdev.biblereaderplus.di.modules.presentation.favorite.FavoriteVersetDetailModule
import com.crskdev.biblereaderplus.di.modules.presentation.favorite.FavoriteVersetsModule
import com.crskdev.biblereaderplus.di.modules.presentation.read.ReadModule
import com.crskdev.biblereaderplus.di.modules.presentation.setup.SetupModule
import com.crskdev.biblereaderplus.di.scopes.PerActivity
import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.presentation.common.CharSequenceTransformerFactory
import com.crskdev.biblereaderplus.presentation.common.HighLightContentTransformer
import com.crskdev.biblereaderplus.presentation.common.IconAtEndTransformer
import com.crskdev.biblereaderplus.presentation.common.LeadFirstLineTransformer
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetDetailFragment
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetsFragment
import com.crskdev.biblereaderplus.presentation.read.ReadFragment
import com.crskdev.biblereaderplus.presentation.setup.SetupFragment
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

/**
 * Created by Cristian Pela on 05.11.2018.
 */

@Module
abstract class SingleActivityModule {

    @PerFragment
    @ContributesAndroidInjector(modules = [SetupModule::class])
    abstract fun setupFragmentInjector(): SetupFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [ReadModule::class])
    abstract fun readFragmentInjector(): ReadFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [FavoriteVersetsModule::class])
    abstract fun favoriteVersetsFragmentInjector(): FavoriteVersetsFragment

    @PerFragment
    @ContributesAndroidInjector(modules = [FavoriteVersetDetailModule::class])
    abstract fun favoriteVersetDetailFragmentInjector(): FavoriteVersetDetailFragment

    @Module
    companion object {
        @JvmStatic
        @PerActivity
        @Provides
        fun provideSpanTransformers(context: Context): CharSequenceTransformerFactory =
            CharSequenceTransformerFactory(
                enumMap {
                    this[CharSequenceTransformerFactory.Type.ICON_AT_END] = lazy {
                        IconAtEndTransformer(context, R.drawable.ic_star_black_10dp)
                    }
                    this[CharSequenceTransformerFactory.Type.LEAD_FIRST_LINE] = lazy {
                        LeadFirstLineTransformer()
                    }
                    this[CharSequenceTransformerFactory.Type.HIGHLIGHT] = lazy {
                        HighLightContentTransformer()
                    }
                }
            )
    }

}



