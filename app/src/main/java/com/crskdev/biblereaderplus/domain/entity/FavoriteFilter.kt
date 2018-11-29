/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.entity

data class FavoriteFilter(val query: String? = null, val tags: List<Tag> = emptyList(), val asc: Boolean = false) {
    companion object {
        val NONE = FavoriteFilter()
        val NONE_ASC = FavoriteFilter(asc = true)
    }
}