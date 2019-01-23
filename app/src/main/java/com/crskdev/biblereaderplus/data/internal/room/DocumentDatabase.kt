/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.data.internal.room

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Created by Cristian Pela on 21.01.2019.
 */
@Database(
    entities = [ReadDB::class, TagDB::class, VersetTagDB::class],
    exportSchema = false,
    version = 1
)
abstract class DocumentDatabase : RoomDatabase() {

    abstract fun readDAO(): ReadDAO

    abstract fun tagDAO(): TagDAO

}