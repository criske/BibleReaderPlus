/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.tag

import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.entity.TagOp
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import com.crskdev.biblereaderplus.domain.gateway.RemoteDocumentRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * Created by Cristian Pela on 13.11.2018.
 */
interface TagOpsInteractor {

    suspend fun request(tagOp: TagOp, responseError: (ResponseError) -> Unit)

    @Suppress("MemberVisibilityCanBePrivate")
    sealed class ResponseError(val err: Throwable?) : Throwable(err) {
        object EmptyTagName : ResponseError(null)
        object ShortTagName : ResponseError(null)
        class Unknown(err: Throwable) : ResponseError(err)
    }

}

class TagOpsInteractorImpl(
    private val dispatchers: GatewayDispatchers,
    private val localRepository: DocumentRepository,
    private val remoteRepository: RemoteDocumentRepository) : TagOpsInteractor {

    override suspend fun request(tagOp: TagOp, responseError: (TagOpsInteractor.ResponseError) -> Unit) =
        supervisorScope {
            val coroutineErrHandler = CoroutineExceptionHandler { _, throwable ->
                if (throwable is TagOpsInteractor.ResponseError) {
                    responseError(throwable)
                } else {
                    responseError(TagOpsInteractor.ResponseError.Unknown(throwable))
                }
            }
            //@formatter:off
            launch(coroutineErrHandler + dispatchers.DEFAULT) {
                //todo: support for remote
                when (tagOp) {
                    is TagOp.Delete -> localRepository.tagDelete(tagOp.id)
                    is TagOp.Rename -> {
                        when {
                            tagOp.newName.isBlank() -> throw TagOpsInteractor.ResponseError.EmptyTagName
                            tagOp.newName.length < 3 -> throw TagOpsInteractor.ResponseError.ShortTagName
                            else -> localRepository.tagRename(tagOp.id, tagOp.newName.trim())
                        }
                    }
                    is TagOp.Color  -> localRepository.tagColor(tagOp.id, tagOp.color)
                    is TagOp.Create -> localRepository.tagCreate(Tag.crateTransientTag(tagOp.name))
                }
            }
            //@formatter:on

            Unit
        }


}