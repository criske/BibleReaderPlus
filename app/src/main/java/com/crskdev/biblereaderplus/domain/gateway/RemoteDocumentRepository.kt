/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.gateway

import com.crskdev.biblereaderplus.domain.entity.ModifiedAt
import com.crskdev.biblereaderplus.domain.entity.RemoteVerset
import com.crskdev.biblereaderplus.domain.entity.Tag

/**
 * Created by Cristian Pela on 02.12.2018.
 */
interface RemoteDocumentRepository {

    fun getAllFavorites(): List<RemoteVerset>

    fun getAllTags(): List<Tag>

    fun favoriteAction(versetId: Int, add: Boolean, modifiedAt: ModifiedAt)

    fun tagFavoriteVerset(add: Boolean, versetId: Int, tagId: String, modifiedAt: ModifiedAt)

    fun tagDelete(id: String)

    fun tagRename(id: String, newName: String)

    fun tagColor(id: String, color: String)

    fun tagCreate(vararg newTags: Tag): List<String>
}

