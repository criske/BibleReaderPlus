/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.interactors.favorite

import com.crskdev.biblereaderplus.domain.entity.SelectedVerset
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import kotlinx.coroutines.coroutineScope

/**
 * Created by Cristian Pela on 07.12.2018.
 */
interface FavoriteVersetInteractor {

    suspend fun request(versetId: Int, response: (SelectedVerset) -> Unit)

}

class FavoriteVersetInteractorImpl(
    private val dispatchers: GatewayDispatchers,
    private val localRepository: DocumentRepository) : FavoriteVersetInteractor {

    override suspend fun request(versetId: Int, response: (SelectedVerset) -> Unit) =
        coroutineScope {
            localRepository.observeVerset(versetId, response)
    }

}