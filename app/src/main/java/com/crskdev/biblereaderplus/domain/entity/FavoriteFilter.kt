/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.entity

data class FavoriteFilter(val query: String? = null, val tags: Set<Tag> = emptySet(), val asc: Boolean = false) {
    companion object {
        val NONE = FavoriteFilter()
    }
}