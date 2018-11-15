/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.gateway

import androidx.paging.PagedList
import com.crskdev.biblereaderplus.domain.entity.Read

/**
 * Created by Cristian Pela on 06.11.2018.
 */
interface DocumentRepository {

    fun save(reads: List<Read>)

    fun read(reader: (PagedList<Read>) -> Unit)

    fun contents(): List<Read.Content>

    fun filter(query: String): List<Read.Content>

}