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
@Parcelize
class ParcelableFavoriteFilter(val query: String? = null,
                               val tags: List<ParcelableTag> = emptyList(),
                               val asc: Boolean = false) : Parcelable

@Parcelize
class ParcelableTag(val id: Int, val name: String, val color: String) : Parcelable

@Suppress("IMPLICIT_CAST_TO_ANY")
fun FavoriteFilter.parcelize(): ParcelableFavoriteFilter =
    ParcelableFavoriteFilter(query, tags.map {
        ParcelableTag(it.id, it.name, it.color)
    }, asc)


fun ParcelableFavoriteFilter.deparcelize(): FavoriteFilter =
    FavoriteFilter(query, tags.map {
        Tag(it.id, it.name, it.color)
    }, asc)