/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.gateway

import androidx.paging.PagedList
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.entity.SelectedVerset
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.entity.VersetProps

/**
 * Created by Cristian Pela on 06.11.2018.
 */
interface DocumentRepository {

    fun save(reads: List<Read>)

    fun read(reader: (PagedList<Read>) -> Unit)

    fun contents(): List<Read.Content>

    fun filter(query: String): List<Read.Content>

    //local
    fun getVerset(versetKey: VersetKey): SelectedVerset?

    //remote?
    fun getVersetProps(versetKey: VersetKey): VersetProps

}