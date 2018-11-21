/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.favorite

import androidx.paging.PagedList
import com.crskdev.arch.coroutines.paging.onPaging
import com.crskdev.arch.coroutines.paging.setupPagedListBuilder
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
    suspend fun request(filter: ReceiveChannel<FavoriteFilter>, response: (PagedList<Read.Verset>) -> Unit)
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class FetchFavoriteVersetsInteractorImpl @Inject constructor(
    private val dispatchers: GatewayDispatchers,
    private val repository: DocumentRepository) : FetchFavoriteVersetsInteractor {

    override suspend fun request(filter: ReceiveChannel<FavoriteFilter>, response: (PagedList<Read.Verset>) -> Unit) =
        coroutineScope {
            val sendChannel = actor<PagedList<Read.Verset>> {
                for (r in channel) {
                    response(r)
                }
            }
            launch {
                var job = Job()
                while (isActive) {
                    select<Unit> {
                        filter.onReceive {
                            job.cancel()
                            job = CoroutineScope(this@launch.coroutineContext + SupervisorJob()).launch {
                                repository.favorites(it)
                                    .setupPagedListBuilder {
                                        config(10)
                                        fetchDispatcher = dispatchers.DEFAULT
                                    }
                                    .onPaging { page, _ ->
                                        sendChannel.offer(page)
                                    }
                            }
                        }
                    }
                }
            }
            Unit
        }

}