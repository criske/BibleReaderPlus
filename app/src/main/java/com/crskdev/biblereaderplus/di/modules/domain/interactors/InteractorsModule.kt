/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.domain.interactors

import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import com.crskdev.biblereaderplus.domain.interactors.favorite.FetchFavoriteVersetsInteractor
import com.crskdev.biblereaderplus.domain.interactors.favorite.FetchFavoriteVersetsInteractorImpl
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Created by Cristian Pela on 21.11.2018.
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@Module
class InteractorsModule {

    @Provides
    fun provideFetchFavoriteVersetsInteractor(dispatchers: GatewayDispatchers,
                                              repository: DocumentRepository): FetchFavoriteVersetsInteractor =
        FetchFavoriteVersetsInteractorImpl(dispatchers, repository)


}