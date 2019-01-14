/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.interactors.favorite

import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import com.crskdev.biblereaderplus.domain.gateway.RemoteDocumentRepository
import com.crskdev.biblereaderplus.domain.interactors.favorite.FavoriteActionsVersetInteractor.Action
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Cristian Pela on 13.11.2018.
 */
interface FavoriteActionsVersetInteractor {

    suspend fun request(versetId: Int, action: Action)

    sealed class Action {
        class Favorite(val add: Boolean) : Action()
        class TagToFavorite(val tagId: String, val add: Boolean) : Action()
    }

}

class FavoriteActionsVersetInteractorImpl @Inject constructor(
    private val dispatchers: GatewayDispatchers,
    private val localRepository: DocumentRepository,
    private val remoteRepository: RemoteDocumentRepository) : FavoriteActionsVersetInteractor {

    @ObsoleteCoroutinesApi
    override suspend fun request(versetId: Int, action: Action) =
        coroutineScope {
            when (action) {
                is Action.Favorite -> {
                    launch(dispatchers.DEFAULT) {
                        localRepository.favoriteAction(action.add, versetId)
                    }
                    launch(dispatchers.IO) {
                        remoteRepository.favoriteAction(versetId, action.add)
                    }
                }
                //@formatter:off
                is Action.TagToFavorite -> {
                    //todo remote support
                    launch(dispatchers.IO) {
                        remoteRepository.tagFavoriteVerset(action.add, versetId, action.tagId)
                    }
                    launch( dispatchers.DEFAULT) {
                        localRepository.tagFavoriteVerset(action.add, versetId, action.tagId)
                    }
                }
                //@formatter:on
            }
            Unit
        }
}




