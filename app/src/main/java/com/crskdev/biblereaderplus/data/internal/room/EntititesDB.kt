/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.data.internal.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Created by Cristian Pela on 21.01.2019.
 */

object TableNames {
    const val READ = "read"
    const val TAGS = "tags"
    const val VERSET_TAGS = "versetTags"
}

@Entity(tableName = TableNames.READ)
class ReadDB {
    @PrimaryKey
    var id: Int = 0
    var bookId: Int = -1
    var chapterId: Int = -1
    var versetId: Int = -1
    var type: Int = -1
    var content: String? = null
    var name: String? = null
    var number: Int = -1
    var chapterNumber: Int = -1
    var abbreviation: String? = null
    var isFavorite: Boolean = false
    lateinit var modifiedAt: String

    companion object {
        const val TYPE_BOOK = 0
        const val TYPE_CHAPTER = 1
        const val TYPE_VERSET = 2
    }
}

@Entity(tableName = TableNames.TAGS, indices = [Index("name", unique = true)])
class TagDB {
    @PrimaryKey
    lateinit var id: String
    lateinit var name: String
    lateinit var color: String
}

@Entity(
    tableName = TableNames.VERSET_TAGS,
    foreignKeys = [
        ForeignKey(
            entity = ReadDB::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("versetId"),
            onDelete = ForeignKey.CASCADE
        ), ForeignKey(
            entity = TagDB::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("tagId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class VersetTagDB {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var versetId: Int = -1
    lateinit var tagId: String
}


class VersetDB {
    var id: Int = -1
    var bookId: Int = -1
    var chapterId: Int = -1
    var number: Int = -1
    var chapterNumber: Int = -1
    var abbreviation: String? = null
    lateinit var content: String
    var isFavorite: Boolean = false
    var tags: List<TagDB>? = null
}


