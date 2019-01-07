/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.di.modules.domain.interactors

import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import com.crskdev.biblereaderplus.domain.gateway.RemoteDocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.SetupCheckService
import com.crskdev.biblereaderplus.domain.interactors.favorite.*
import com.crskdev.biblereaderplus.domain.interactors.read.ContentInteractor
import com.crskdev.biblereaderplus.domain.interactors.read.ContentInteractorImpl
import com.crskdev.biblereaderplus.domain.interactors.read.ReadInteractor
import com.crskdev.biblereaderplus.domain.interactors.read.ReadInteractorImpl
import com.crskdev.biblereaderplus.domain.interactors.setup.CheckInitInteractor
import com.crskdev.biblereaderplus.domain.interactors.setup.CheckInitInteractorImpl
import com.crskdev.biblereaderplus.domain.interactors.tag.FetchTagsInteractor
import com.crskdev.biblereaderplus.domain.interactors.tag.FetchTagsInteractorImpl
import com.crskdev.biblereaderplus.domain.interactors.tag.TagOpsInteractor
import com.crskdev.biblereaderplus.domain.interactors.tag.TagOpsInteractorImpl
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
    fun provideCheckInitInteractor(setupCheckService: SetupCheckService): CheckInitInteractor =
        CheckInitInteractorImpl(setupCheckService)

    @Provides
    fun provideReadAllInteractor(dispatchers: GatewayDispatchers,
                                 repository: DocumentRepository): ReadInteractor =
        ReadInteractorImpl(dispatchers, repository)

    @Provides
    fun provideContentsInteractor(dispatchers: GatewayDispatchers,
                                  repository: DocumentRepository): ContentInteractor =
        ContentInteractorImpl(dispatchers, repository)

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

    @Provides
    fun provideTagsOpsInteractor(dispatchers: GatewayDispatchers, localRepository: DocumentRepository, remoteRepository: RemoteDocumentRepository): TagOpsInteractor =
        TagOpsInteractorImpl(dispatchers, localRepository, remoteRepository)

}