/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.entity

/**
 * Created by Cristian Pela on 07.11.2018.
 */
sealed class Read(val id: Int) {

    sealed class Content(id: Int) : Read(id) {

        class Book(id: Int, val name: String, val abbreviation: String) : Content(id)

        class Chapter(val key: ChapterKey, val number: Int) : Content(key.id) {
            data class Key(val id: Int, val bookId: Int)
        }

    }

    data class Verset(val key: VersetKey,
                      val number: Int,
                      val bookName: String,
                      val chapterNumber: Int,
                      val content: CharSequence,
                      val isFavorite: Boolean,
                      val modifiedAt: ModifiedAt) : Read(key.id) {
        data class Key(val id: Int, val bookId: Int, val chapterId: Int, val remoteKey: String) {
            companion object {
                val NONE = VersetKey(-1, -1, -1, "")
            }
        }
    }
}

class RemoteVerset(var id: Int = -1, var tagIds: List<String> = emptyList())

inline class ModifiedAt(val date: String)

typealias VersetKey = Read.Verset.Key
typealias ChapterKey = Read.Content.Chapter.Key