/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.favorite

import androidx.paging.PagedList
import com.crskdev.biblereaderplus.common.util.pagedlist.onPagingWithDefaultPagedListBuilder
import com.crskdev.biblereaderplus.domain.entity.FavoriteFilter
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.selects.select
import javax.inject.Inject

/**
 * Created by Cristian Pela on 13.11.2018.
 */
interface FetchFavoriteVersetsInteractor {
    suspend fun request(filter: ReceiveChannel<FavoriteFilter>,
                        decorator: (FavoriteFilter, Read.Verset) -> Read.Verset = { _, v -> v },
                        response: (PagedList<Read.Verset>) -> Unit)
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class FetchFavoriteVersetsInteractorImpl @Inject constructor(
    private val dispatchers: GatewayDispatchers,
    private val repository: DocumentRepository) : FetchFavoriteVersetsInteractor {

    override suspend fun request(filter: ReceiveChannel<FavoriteFilter>,
                                 decorator: (FavoriteFilter, Read.Verset) -> Read.Verset,
                                 response: (PagedList<Read.Verset>) -> Unit) =
        coroutineScope {
            val sendChannel = actor<PagedList<Read.Verset>> {
                for (r in channel) {
                    response(r)
                }
            }
            sendChannel.invokeOnClose {
                println("offered not anymore $it")
            }
            launch(SupervisorJob()) {
                var job = Job()
                while (!sendChannel.isClosedForSend) {
                    select<Unit> {
                        filter.onReceive { it ->
                            job.cancel()
                            job = Job()
                            launch(job) {
                                repository.favorites(it)
                                    .mapByPage { l -> l.map { v -> decorator(it, v) } }
                                    .onPagingWithDefaultPagedListBuilder(dispatchers.DEFAULT) {
                                        sendChannel.offer(it)
                                    }
                            }
                        }
                    }
                }
            }
            Unit
        }

}