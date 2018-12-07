/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.di.modules.domain.interactors

import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import com.crskdev.biblereaderplus.domain.gateway.RemoteDocumentRepository
import com.crskdev.biblereaderplus.domain.interactors.favorite.*
import com.crskdev.biblereaderplus.domain.interactors.tag.FetchTagsInteractor
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

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

    //TODO real impl
    @Provides
    fun provideFetchTagsInteractor(dispatchers: GatewayDispatchers): FetchTagsInteractor =
        object : FetchTagsInteractor {
            val colors = listOf(
                "#ffcc99", "#cc66ff", "#f7ffe6", "#80aaff", "#ff66a3", "#ffffff", "#000000"
            )
            val tagPrefixes = listOf(
                "", "a", "abc", "abcd", "abdcdefg"
            )
            val tagsMock = (0..100).map {
                Tag(it + 1, "Tag${it + 1}${tagPrefixes.random()}", colors.random())
            }

            override suspend fun request(query: String): List<Tag> = coroutineScope {
                withContext(dispatchers.DEFAULT) {
                    tagsMock.filter { it.name.contains(query, ignoreCase = true) }
                }
            }
        }

}