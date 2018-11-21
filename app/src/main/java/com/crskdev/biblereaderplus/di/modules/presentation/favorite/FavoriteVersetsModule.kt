/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */
package com.crskdev.biblereaderplus.di.modules.presentation.favorite

import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetsFragment
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetsViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.viewModelFromProvider
import dagger.Module
import dagger.Provides

/**
 * Created by Cristian Pela on 21.11.2018.
 */
@Module
abstract class FavoriteVersetsModule {

    @Module
    companion object {

        @JvmStatic
        @PerFragment
        @Provides
        fun provideFavoriteVersetsViewModel(dispatchers: GatewayDispatchers, container: FavoriteVersetsFragment): FavoriteVersetsViewModel =
            viewModelFromProvider(container) { FavoriteVersetsViewModel(dispatchers.MAIN) }

    }
}