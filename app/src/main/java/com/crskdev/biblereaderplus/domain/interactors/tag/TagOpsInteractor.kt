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
import com.crskdev.biblereaderplus.domain.interactors.tag.TagOpsInteractor.ResponseError.EmptyTagName
import com.crskdev.biblereaderplus.domain.interactors.tag.TagOpsInteractor.ResponseError.ShortTagName
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Created by Cristian Pela on 13.11.2018.
 */
interface TagOpsInteractor {

    suspend fun request(tagOp: TagOp, responseError: (ResponseError) -> Unit)

    @Suppress("MemberVisibilityCanBePrivate")
    sealed class ResponseError(val err: Throwable?) : Throwable(err) {
        object EmptyTagName : ResponseError(null)
        class ShortTagName(val name: String, val lengthRequired: Int) : ResponseError(null)
        class Unknown(err: Throwable) : ResponseError(err)
    }

}

class TagOpsInteractorImpl(
    private val dispatchers: GatewayDispatchers,
    private val localRepository: DocumentRepository,
    private val remoteRepository: RemoteDocumentRepository) : TagOpsInteractor {

    companion object {
        private const val VALIDATION_TAG_NAME_LENGTH = 3
    }

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
                        validateUpsert(tagOp.newName){
                            launch(dispatchers.IO) {
                                //todo: remote rename
                                println("TagOpsInteractor: Remote rename for tag-id: ${tagOp.id}. New name: $it")
                            }
                            println("TagOpsInteractor: Local rename for tag-id: ${tagOp.id}. New name: $it")
                            localRepository.tagRename(tagOp.id, tagOp.newName.trim())
                        }
                    }
                    is TagOp.Color  -> localRepository.tagColor(tagOp.id, tagOp.color)
                    is TagOp.Create -> {
                         validateUpsert(tagOp.name){
                             val remoteCreatedId = withContext(dispatchers.IO) {
                                  //todo create remote first
                                 val remoteId = UUID.randomUUID().toString()
                                  println("TagOpsInteractor: Remote create for tag with name: $it and id: $remoteId")
                                 remoteId
                             }
                             println("TagOpsInteractor: Local create for tag with name: $it and remote id: $remoteCreatedId")
                             localRepository.tagCreate(Tag(remoteCreatedId, it))
                         }

                    }
                }
            }
            //@formatter:on
            Unit
        }

    //@formatter:off
    private suspend fun validateUpsert(name: String, upsert: suspend (String) -> Unit) {
        when {
            name.isBlank() -> throw EmptyTagName
            name.length < VALIDATION_TAG_NAME_LENGTH -> throw ShortTagName(name, VALIDATION_TAG_NAME_LENGTH)
            else -> upsert(name)
        }
    }
    //@formatter:on

}