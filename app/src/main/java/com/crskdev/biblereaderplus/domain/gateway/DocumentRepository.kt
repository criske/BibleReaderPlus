/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.gateway

import androidx.paging.PagedList
import com.crskdev.biblereaderplus.domain.entity.Read
import kotlinx.coroutines.channels.SendChannel

/**
 * Created by Cristian Pela on 06.11.2018.
 */
interface DocumentRepository {

    fun save(reads: List<Read>)

    suspend fun read(channel: SendChannel<PagedList<Read>>)

}