/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.gateway

import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.entity.VersetProps

/**
 * Created by Cristian Pela on 02.12.2018.
 */
interface RemoteDocumentRepository {

    fun getAllFavorites(): List<VersetProps>

    fun getAllTags(): List<Tag>

    fun favoriteAction(versetKey: VersetKey, add: Boolean)
}