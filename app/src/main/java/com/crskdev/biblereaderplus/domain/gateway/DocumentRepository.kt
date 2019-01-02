/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.gateway

import androidx.annotation.MainThread
import androidx.paging.DataSource
import com.crskdev.biblereaderplus.domain.entity.*

/**
 * Created by Cristian Pela on 06.11.2018.
 */
interface DocumentRepository {

    fun save(reads: List<Read>)

    fun read(): DataSource.Factory<Int, Read>

    fun contents(): List<Read.Content>

    fun filterContents(contains: String): List<Read.Content>

    //local
    fun getVerset(id: Int): SelectedVerset?

    @MainThread
    suspend fun observeVerset(id: Int, observer: (SelectedVerset) -> Unit)

    //remote?
    fun getVersetProps(versetKey: VersetKey): VersetProps

    fun synchronize()

    fun favoriteAction(id: Int, add: Boolean)

    fun favorites(filter: FavoriteFilter): DataSource.Factory<Int, Read.Verset>

    //##############################TAGS#################################

    @MainThread
    suspend fun tagsObserve(contains: String?, observer: (Set<Tag>) -> Unit)

    fun tagFavoriteVerset(versetId: Int, tagId: String, add: Boolean)

    fun tagDelete(id: String)

    fun tagRename(id: String, newName: String)

    fun tagColor(id: String, color: String)

    fun tagCreate(newTag: Tag)
}