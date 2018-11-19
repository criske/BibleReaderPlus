package com.crskdev.biblereaderplus.domain.interactors.favorite

import androidx.paging.PagedList
import com.crskdev.arch.coroutines.paging.Detachable
import com.crskdev.arch.coroutines.paging.onPaging
import com.crskdev.arch.coroutines.paging.setupPagedListBuilder
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import com.crskdev.biblereaderplus.domain.interactors.favorite.FetchFavoriteVersetsInteractor.Filter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import javax.inject.Inject

/**
 * Created by Cristian Pela on 13.11.2018.
 */
interface FetchFavoriteVersetsInteractor {

    suspend fun request(filter: ReceiveChannel<Filter>, response: (PagedList<Read.Verset>) -> Unit)

    sealed class Filter {
        sealed class ByLastModified : Filter() {
            object ASC : ByLastModified()
            object DESC : ByLastModified()
        }

        object None : Filter()
        class Query(val query: String) : Filter()
        class ByTag(val tag: Tag) : Filter()
    }

}

class FetchFavoriteVersetsInteractorImpl @Inject constructor(
    private val dispatchers: GatewayDispatchers,
    private val repository: DocumentRepository
) : FetchFavoriteVersetsInteractor {

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    override suspend fun request(filter: ReceiveChannel<Filter>, response: (PagedList<Read.Verset>) -> Unit) =
        coroutineScope {
            val sendChannel = actor<PagedList<Read.Verset>> {
                for (r in channel) {
                    response(r)
                }
            }
            launch(dispatchers.DEFAULT) {
                var prevSource: Detachable<Read.Verset>? = null
                while (true) {
                    select<Unit> {
                        filter.onReceive {
                            prevSource?.detach()
                            prevSource = repository.favorites()
                                .setupPagedListBuilder(10)
                                .onPaging {
                                    launch { sendChannel.send(it) }
                                }
                        }
                    }
                }
            }
            Unit
        }

}