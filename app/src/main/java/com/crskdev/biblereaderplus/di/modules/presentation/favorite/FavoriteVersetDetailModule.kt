/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.presentation.favorite

import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.domain.interactors.favorite.FavoriteActionsVersetInteractor
import com.crskdev.biblereaderplus.domain.interactors.favorite.FavoriteVersetInteractor
import com.crskdev.biblereaderplus.presentation.common.CharSequenceTransformerFactory
import com.crskdev.biblereaderplus.presentation.common.deparcelize
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetDetailFragment
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetDetailFragmentArgs
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetDetailViewModel
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetDetailViewModelImpl
import com.crskdev.biblereaderplus.presentation.util.arch.viewModelFromProvider
import dagger.Module
import dagger.Provides

/**
 * Created by Cristian Pela on 02.12.2018.
 */
@Module
class FavoriteVersetDetailModule {

    @PerFragment
    @Provides
    fun provideFavoriteVersetsViewModel(container: FavoriteVersetDetailFragment,
                                        favoriteActionsVersetInteractor: FavoriteActionsVersetInteractor,
                                        favoriteVersetInteractor: FavoriteVersetInteractor,
                                        charSequenceTransformerFactory: CharSequenceTransformerFactory)
            : FavoriteVersetDetailViewModel =
        viewModelFromProvider(container) {
            FavoriteVersetDetailViewModelImpl(
                FavoriteVersetDetailFragmentArgs.fromBundle(container.arguments).key.deparcelize(),
                favoriteActionsVersetInteractor,
                favoriteVersetInteractor,
                charSequenceTransformerFactory
            )
        }

}