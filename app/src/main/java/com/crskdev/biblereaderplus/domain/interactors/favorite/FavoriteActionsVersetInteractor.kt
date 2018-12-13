/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.favorite

import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.entity.TagOp
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import com.crskdev.biblereaderplus.domain.gateway.RemoteDocumentRepository
import com.crskdev.biblereaderplus.domain.interactors.favorite.FavoriteActionsVersetInteractor.ResponseError
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

/**
 * Created by Cristian Pela on 13.11.2018.
 */
interface FavoriteActionsVersetInteractor {

    suspend fun request(versetKey: VersetKey, action: Action, responseError: (ResponseError) -> Unit)

    sealed class Action {
        class FavoriteAction(val add: Boolean) : Action()
        class TagAction(val tagOp: TagOp) : Action()
    }

    sealed class ResponseError(val err: Throwable?) : Throwable(err) {
        object EmptyTagName : ResponseError(null)
        object ShortTagName : ResponseError(null)
        class Unknown(err: Throwable) : ResponseError(err)
    }

}

class FavoriteActionsVersetInteractorImpl @Inject constructor(
    private val dispatchers: GatewayDispatchers,
    private val localRepository: DocumentRepository,
    private val remoteRepository: RemoteDocumentRepository) : FavoriteActionsVersetInteractor {

    @ObsoleteCoroutinesApi
    override suspend fun request(versetKey: VersetKey, action: FavoriteActionsVersetInteractor.Action,
                                 responseError: (FavoriteActionsVersetInteractor.ResponseError) -> Unit) =
        supervisorScope {
            val coroutineErrHandler = CoroutineExceptionHandler { _, throwable ->
                if (throwable is ResponseError) {
                    responseError(throwable)
                } else {
                    responseError(ResponseError.Unknown(throwable))
                }
            }
            when (action) {
                is FavoriteActionsVersetInteractor.Action.FavoriteAction -> {
                    launch(coroutineErrHandler + dispatchers.DEFAULT) {
                        localRepository.favoriteAction(versetKey, action.add)
                    }
                    launch(coroutineErrHandler + dispatchers.IO) {
                        remoteRepository.favoriteAction(versetKey, action.add)
                    }
                }
                //@formatter:off
                is FavoriteActionsVersetInteractor.Action.TagAction -> {
                    launch(coroutineErrHandler + dispatchers.DEFAULT) {
                        val tagOp = action.tagOp
                         //todo: support for remote
                        when (tagOp) {
                            is TagOp.Add    -> localRepository.tagFavoriteVerset(versetKey, tagOp.id, true)
                            is TagOp.Remove -> localRepository.tagFavoriteVerset(versetKey, tagOp.id, false)
                            is TagOp.Delete -> localRepository.tagDelete(tagOp.id)
                            is TagOp.Rename -> {
                                when {
                                    tagOp.newName.isBlank() -> throw ResponseError.EmptyTagName
                                    tagOp.newName.length < 3 -> throw ResponseError.ShortTagName
                                    else -> localRepository.tagRename(tagOp.id, tagOp.newName.trim())
                                }
                            }
                            is TagOp.Color  -> localRepository.tagColor(tagOp.id, tagOp.color)
                            is TagOp.Create -> localRepository.tagCreate(Tag("",tagOp.name))
                        }
                    }
                }
                //@formatter:on
            }

            Unit
        }

}


