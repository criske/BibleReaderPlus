/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.entity

sealed class FavoriteFilter(val asc: Boolean) {
    class None(asc: Boolean = false) : FavoriteFilter(asc)
    class Query(val query: String, asc: Boolean = false) : FavoriteFilter(asc)
    class ByTag(val tag: Tag, asc: Boolean = false) : FavoriteFilter(asc)
}