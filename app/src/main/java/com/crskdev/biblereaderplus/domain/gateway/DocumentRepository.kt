/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.gateway

import androidx.annotation.MainThread
import androidx.paging.DataSource
import androidx.paging.PagedList
import com.crskdev.biblereaderplus.domain.entity.*

/**
 * Created by Cristian Pela on 06.11.2018.
 */
interface DocumentRepository {

    fun save(reads: List<Read>)

    fun read(reader: (PagedList<Read>) -> Unit)

    fun contents(): List<Read.Content>

    fun filter(contains: String): List<Read.Content>

    //local
    fun getVerset(versetKey: VersetKey): SelectedVerset?

    @MainThread
    suspend fun observeVerset(versetKey: VersetKey, observer: (SelectedVerset) -> Unit)

    //remote?
    fun getVersetProps(versetKey: VersetKey): VersetProps

    fun synchronize()

    fun favoriteAction(versetKey: VersetKey, add: Boolean)

    fun favorites(filter: FavoriteFilter): DataSource.Factory<Int, Read.Verset>

    //##############################TAGS#################################

    @MainThread
    suspend fun tagsObserve(contains: String?, observer: (Set<Tag>) -> Unit)

    fun tagFavoriteVerset(versetKey: VersetKey, tagId: String, add: Boolean)

    fun tagDelete(id: String)

    fun tagRename(id: String, newName: String)

    fun tagColor(id: String, color: String)

    fun tagCreate(newTag: Tag)
}