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

    fun runTransaction(block: DocumentRepository.() -> Unit)

    fun save(reads: List<Read>)

    fun read(): DataSource.Factory<Int, Read>

    fun contents(): List<Read.Content>

    fun filterContents(contains: String): List<Read.Content>

    //##############################VERSET#################################

    fun getVerset(id: Int): SelectedVerset?

    @MainThread
    suspend fun observeVerset(id: Int, observer: (SelectedVerset) -> Unit)

    fun favoriteAction(add: Boolean, id: Int, modifiedAt: ModifiedAt)

    fun favoriteActionBatch(add: Boolean, vararg ids: Pair<Int, ModifiedAt>)

    fun favorites(filter: FavoriteFilter): DataSource.Factory<Int, Read.Verset>

    //##############################TAGS#################################

    @MainThread
    suspend fun tagsObserve(contains: String?, observer: (Set<Tag>) -> Unit)

    fun tagFavoriteVerset(add: Boolean, versetId: Int, tagId: String, modifiedAt: ModifiedAt)

    fun tagFavoriteVersetBatch(add: Boolean, versetId: Int, modifiedAt: ModifiedAt, vararg tagIds: String)

    fun tagDelete(id: String)

    fun tagRename(id: String, newName: String)

    fun tagColor(id: String, color: String)

    fun tagCreate(vararg newTags: Tag)

}