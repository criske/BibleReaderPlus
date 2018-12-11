/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.favorite

import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import com.crskdev.biblereaderplus.domain.gateway.RemoteDocumentRepository
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Cristian Pela on 13.11.2018.
 */
interface FavoriteActionsVersetInteractor {

    suspend fun request(versetKey: VersetKey, action: Action)

    sealed class Action {
        class FavoriteAction(val add: Boolean) : Action()
        class TagAction(val tagId: String, val add: Boolean) : Action()
    }

}

class FavoriteActionsVersetInteractorImpl @Inject constructor(
    private val dispatchers: GatewayDispatchers,
    private val localRepository: DocumentRepository,
    private val remoteRepository: RemoteDocumentRepository) : FavoriteActionsVersetInteractor {

    @ObsoleteCoroutinesApi
    override suspend fun request(versetKey: VersetKey, action: FavoriteActionsVersetInteractor.Action) =
        coroutineScope {
            when (action) {
                is FavoriteActionsVersetInteractor.Action.FavoriteAction -> {
                    launch(dispatchers.DEFAULT) {
                        localRepository.favoriteAction(versetKey, action.add)
                    }
                    launch(dispatchers.IO) {
                        remoteRepository.favoriteAction(versetKey, action.add)
                    }
                }
                is FavoriteActionsVersetInteractor.Action.TagAction -> {
                    launch(dispatchers.DEFAULT) {
                        localRepository.tagToVersetAction(versetKey, action.tagId, action.add)
                    }
                    //todo: support for remote
                }
            }

            Unit
        }

}


