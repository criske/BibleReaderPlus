/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.data

import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.entity.VersetProps
import com.crskdev.biblereaderplus.domain.gateway.RemoteDocumentRepository

/**
 * Created by Cristian Pela on 02.12.2018.
 */
class RemoteDocumentRepositoryImpl : RemoteDocumentRepository {

    override fun getAllFavorites(): List<VersetProps> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllTags(): List<Tag> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun favoriteAction(versetKey: VersetKey, add: Boolean) {
        //TODO: implement favorite Action
    }

}