/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.common

import android.os.Parcelable
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import kotlinx.android.parcel.Parcelize

/**
 * Created by Cristian Pela on 02.12.2018.
 */
@Parcelize
data class ParcelableVersetKey(val id: Int, val bookId: Int, val chapterId: Int, val remoteKey: String) :
    Parcelable {
}

fun VersetKey.parcelize(): ParcelableVersetKey =
    ParcelableVersetKey(id, bookId, chapterId, remoteKey)

fun ParcelableVersetKey.deparcelize(): VersetKey = VersetKey(id, bookId, chapterId, remoteKey)