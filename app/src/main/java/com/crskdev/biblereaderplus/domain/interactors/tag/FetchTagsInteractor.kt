/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.interactors.tag

import com.crskdev.biblereaderplus.domain.entity.Tag

/**
 * Created by Cristian Pela on 13.11.2018.
 */
interface FetchTagsInteractor {

    suspend fun request(query: String): List<Tag>

}