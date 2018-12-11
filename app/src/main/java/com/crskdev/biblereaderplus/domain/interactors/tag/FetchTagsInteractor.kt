/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.tag

import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Created by Cristian Pela on 13.11.2018.
 */
interface FetchTagsInteractor {

    suspend fun request(contains: String?): List<Tag>

}

class FetchTagsInteractorImpl(
    private val dispatchers: GatewayDispatchers,
    private val repository: DocumentRepository) : FetchTagsInteractor {

    override suspend fun request(contains: String?): List<Tag> = coroutineScope {
        withContext(dispatchers.DEFAULT) {
            repository.filterTags(contains)
        }
    }
}