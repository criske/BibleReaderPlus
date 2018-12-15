/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */
package com.crskdev.biblereaderplus.di.modules.presentation.favorite

import com.crskdev.biblereaderplus.di.modules.presentation.tag.TagOpsModule
import com.crskdev.biblereaderplus.di.scopes.PerChildFragment
import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import com.crskdev.biblereaderplus.domain.interactors.favorite.FavoriteActionsVersetInteractor
import com.crskdev.biblereaderplus.domain.interactors.favorite.FetchFavoriteVersetsInteractor
import com.crskdev.biblereaderplus.domain.interactors.tag.FetchTagsInteractor
import com.crskdev.biblereaderplus.domain.interactors.tag.TagOpsInteractor
import com.crskdev.biblereaderplus.presentation.common.CharSequenceTransformerFactory
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetsFragment
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetsViewModel
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetsViewModelImpl
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
 * Created by Cristian Pela on 21.11.2018.
 */
@Module
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
abstract class FavoriteVersetsModule {

    @PerChildFragment
    @ContributesAndroidInjector(modules = [TagOpsModule::class])
    abstract fun tagsSearchFragmentInjector(): TagsSearchBottomSheetDialogFragment

    @Module
    companion object {

        @JvmStatic
        @PerFragment
        @Provides
        fun provideFavoriteVersetsViewModel(container: FavoriteVersetsFragment,
                                            dispatchers: GatewayDispatchers,
                                            transformerFactory: CharSequenceTransformerFactory,
                                            versetsInteractor: FetchFavoriteVersetsInteractor,
                                            favoriteActionsVersetInteractor: FavoriteActionsVersetInteractor,
                                            fetchTagsInteractor: FetchTagsInteractor): FavoriteVersetsViewModel =
            viewModelFromProvider(container) {
                FavoriteVersetsViewModelImpl(
                    dispatchers.MAIN,
                    transformerFactory,
                    versetsInteractor,
                    fetchTagsInteractor,
                    favoriteActionsVersetInteractor
                )
            }

        @JvmStatic
        @PerFragment
        @Provides
        fun provideTagSelectedViewModel(container: FavoriteVersetsFragment): TagSelectViewModel =
            viewModelFromProvider(container) {
                TagSelectViewModel()
            }

        @JvmStatic
        @PerFragment
        @Provides
        fun provideTagOpsViewModel(container: FavoriteVersetsFragment,
                                   fetchTagsInteractor: FetchTagsInteractor,
                                   tagOpsInteractor: TagOpsInteractor): TagsOpsViewModel =
            viewModelFromProvider(container) {
                TagsOpsViewModel(fetchTagsInteractor, tagOpsInteractor)
            }
    }

}
