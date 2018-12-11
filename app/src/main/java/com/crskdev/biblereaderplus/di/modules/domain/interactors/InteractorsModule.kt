/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.domain.interactors

import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import com.crskdev.biblereaderplus.domain.gateway.RemoteDocumentRepository
import com.crskdev.biblereaderplus.domain.interactors.favorite.*
import com.crskdev.biblereaderplus.domain.interactors.tag.FetchTagsInteractor
import com.crskdev.biblereaderplus.domain.interactors.tag.FetchTagsInteractorImpl
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

    @Provides
    fun provideFavoriteActionVersetInteractor(dispatchers: GatewayDispatchers,
                                              localRepository: DocumentRepository,
                                              remoteRepository: RemoteDocumentRepository): FavoriteActionsVersetInteractor =
        FavoriteActionsVersetInteractorImpl(dispatchers, localRepository, remoteRepository)

    @Provides
    fun provideFavoriteVersetInteractor(dispatchers: GatewayDispatchers,
                                        localRepository: DocumentRepository): FavoriteVersetInteractor =
        FavoriteVersetInteractorImpl(dispatchers, localRepository)

    @Provides
    fun provideFetchTagsInteractor(dispatchers: GatewayDispatchers, repository: DocumentRepository): FetchTagsInteractor =
        FetchTagsInteractorImpl(dispatchers, repository)

}