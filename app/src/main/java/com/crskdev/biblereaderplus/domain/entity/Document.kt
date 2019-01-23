/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.entity

/**
 * Created by Cristian Pela on 07.11.2018.
 */
sealed class Read(val id: Int) {

    abstract val modifiedAt: ModifiedAt

    sealed class Content(id: Int, override val modifiedAt: ModifiedAt) : Read(id) {

        class Book(id: Int, val name: String, val abbreviation: String, modifiedAt: ModifiedAt) :
            Content(id, modifiedAt)

        class Chapter(val key: ChapterKey, val number: Int, val bookName: String, modifiedAt: ModifiedAt) :
            Content(key.id, modifiedAt) {
            data class Key(val id: Int, val bookId: Int)
        }

    }

    data class Verset(val key: VersetKey,
                      val number: Int,
                      val bookName: String,
                      val chapterNumber: Int,
                      val content: CharSequence,
                      val isFavorite: Boolean,
                      override val modifiedAt: ModifiedAt) : Read(key.id) {
        data class Key(val id: Int, val bookId: Int, val chapterId: Int, val remoteKey: String) {
            companion object {
                val NONE = VersetKey(-1, -1, -1, "")
            }
        }
    }
}

class RemoteVerset(var id: Int = -1, var modifiedAt: ModifiedAt, var tagIds: List<String> = emptyList())

inline class ModifiedAt(val date: String)

typealias VersetKey = Read.Verset.Key
typealias ChapterKey = Read.Content.Chapter.Key