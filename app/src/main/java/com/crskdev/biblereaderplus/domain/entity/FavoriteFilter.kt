package com.crskdev.biblereaderplus.domain.entity

sealed class FavoriteFilter {
    sealed class ByLastModified : FavoriteFilter() {
        object ASC : ByLastModified()
        object DESC : ByLastModified()
    }

    object None : FavoriteFilter()
    class Query(val query: String) : FavoriteFilter()
    class ByTag(val tag: Tag) : FavoriteFilter()
}