/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.di.modules.presentation.favorite

import com.crskdev.biblereaderplus.di.modules.presentation.tag.TagOpsModule
import com.crskdev.biblereaderplus.di.scopes.PerChildFragment
import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.domain.interactors.favorite.FavoriteActionsVersetInteractor
import com.crskdev.biblereaderplus.domain.interactors.favorite.FavoriteVersetInteractor
import com.crskdev.biblereaderplus.domain.interactors.tag.FetchTagsInteractor
import com.crskdev.biblereaderplus.domain.interactors.tag.TagOpsInteractor
import com.crskdev.biblereaderplus.presentation.common.CharSequenceTransformerFactory
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetDetailFragment
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetDetailFragmentArgs
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetDetailViewModel
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetDetailViewModelImpl
import com.crskdev.biblereaderplus.presentation.tags.TagSelectViewModel
import com.crskdev.biblereaderplus.presentation.tags.TagsOpsViewModel
import com.crskdev.biblereaderplus.presentation.tags.TagsSearchBottomSheetDialogFragment
import com.crskdev.biblereaderplus.presentation.util.arch.viewModelFromProvider
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Created by Cristian Pela on 02.12.2018.
 */
@Module
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
abstract class FavoriteVersetDetailModule {

    @PerChildFragment
    @ContributesAndroidInjector(modules = [TagOpsModule::class])
    abstract fun tagsSearchFragment(): TagsSearchBottomSheetDialogFragment

    @Module
    companion object {

        @JvmStatic
        @PerFragment
        @Provides
        fun provideFavoriteVersetsViewModel(container: FavoriteVersetDetailFragment,
                                            favoriteActionsVersetInteractor: FavoriteActionsVersetInteractor,
                                            favoriteVersetInteractor: FavoriteVersetInteractor,
                                            charSequenceTransformerFactory: CharSequenceTransformerFactory)
                : FavoriteVersetDetailViewModel =
            viewModelFromProvider(container) {
                FavoriteVersetDetailViewModelImpl(
                    FavoriteVersetDetailFragmentArgs.fromBundle(container.arguments).versetId,
                    favoriteActionsVersetInteractor,
                    favoriteVersetInteractor,
                    charSequenceTransformerFactory
                )
            }

        @JvmStatic
        @PerFragment
        @Provides
        fun provideTagSelectedViewModel(container: FavoriteVersetDetailFragment): TagSelectViewModel =
            viewModelFromProvider(container) {
                TagSelectViewModel()
            }

        @JvmStatic
        @PerFragment
        @Provides
        fun provideTagOpsViewModel(container: FavoriteVersetDetailFragment,
                                   fetchTagsInteractor: FetchTagsInteractor,
                                   tagOpsInteractor: TagOpsInteractor): TagsOpsViewModel =
            viewModelFromProvider(container) {
                TagsOpsViewModel(fetchTagsInteractor, tagOpsInteractor)
            }
    }

}