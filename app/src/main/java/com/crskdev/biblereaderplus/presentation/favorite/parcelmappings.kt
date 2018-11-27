/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import android.os.Parcelable
import com.crskdev.biblereaderplus.domain.entity.FavoriteFilter
import com.crskdev.biblereaderplus.domain.entity.Tag
import kotlinx.android.parcel.Parcelize

/**
 * Created by Cristian Pela on 27.11.2018.
 */
sealed class ParcelableFavoriteFilter {
    @Parcelize
    class None(val asc: Boolean = false) : ParcelableFavoriteFilter(), Parcelable

    @Parcelize
    class Query(val query: String, val asc: Boolean = false) : ParcelableFavoriteFilter(),
        Parcelable

    @Parcelize
    class ByTag(val tag: String, val asc: Boolean = false) : ParcelableFavoriteFilter(),
        Parcelable
}

@Suppress("IMPLICIT_CAST_TO_ANY")
fun FavoriteFilter.parcelize(): Parcelable =
    when (this) {
        is FavoriteFilter.None -> ParcelableFavoriteFilter.None(asc)
        is FavoriteFilter.Query -> ParcelableFavoriteFilter.Query(query, asc)
        is FavoriteFilter.ByTag -> ParcelableFavoriteFilter.ByTag(tag.name, asc)
    } as Parcelable

fun ParcelableFavoriteFilter.deparcelize(): FavoriteFilter =
    when (this) {
        is ParcelableFavoriteFilter.None -> FavoriteFilter.None(asc)
        is ParcelableFavoriteFilter.Query -> FavoriteFilter.Query(query, asc)
        is ParcelableFavoriteFilter.ByTag -> FavoriteFilter.ByTag(Tag(tag), asc)
    }