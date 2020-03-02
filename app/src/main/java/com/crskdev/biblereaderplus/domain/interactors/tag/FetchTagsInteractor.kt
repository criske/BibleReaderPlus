/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2020.
 */

package com.crskdev.biblereaderplus.domain.interactors.tag

import com.crskdev.biblereaderplus.common.util.switchSelectOnReceive
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Created by Cristian Pela on 13.11.2018.
 */
interface FetchTagsInteractor {

    suspend fun request(contains: ReceiveChannel<String?>, response: (Set<Tag>) -> Unit)

}

class FetchTagsInteractorImpl(
    private val dispatchers: GatewayDispatchers,
    private val repository: DocumentRepository) : FetchTagsInteractor {
    override suspend fun request(contains: ReceiveChannel<String?>, response: (Set<Tag>) -> Unit) =
        coroutineScope {
            switchSelectOnReceive(contains) { job, what ->
                launch(job + dispatchers.DEFAULT) {
                    repository.tagsObserve(what) {
                        response(it)
                    }
                }
            }

//            var job = Job()
//            while (isActive) {
//                select<Unit> {
//                    contains.onReceive {
//                        job.cancel()
//                        job = launch(dispatchers.DEFAULT) {
//                            repository.tagsObserve(it) {
//                                response(it)
//                            }
//                        }
//                    }
//                }
//            }
        }
}