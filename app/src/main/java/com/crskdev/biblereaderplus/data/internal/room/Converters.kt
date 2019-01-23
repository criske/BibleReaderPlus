/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.data.internal.room

import androidx.room.TypeConverter

/**
 * Created by Cristian Pela on 22.01.2019.
 */
class Converters {

    @TypeConverter
    fun listTagsFromConcatString(value: String?): List<TagDB>? =
        value?.split("&")?.map {
            it.split("$").let {
                TagDB().apply {
                    id = it[0]
                    name = it[1]
                    color = it[2]
                }
            }
        }
}