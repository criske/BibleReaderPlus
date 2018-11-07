/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.gateway

import com.crskdev.biblereaderplus.domain.entity.Document

/**
 * Created by Cristian Pela on 06.11.2018.
 */
interface DocumentRepository {

    fun save(document: Document)

}