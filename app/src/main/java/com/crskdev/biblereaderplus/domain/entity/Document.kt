/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.entity

/**
 * Created by Cristian Pela on 07.11.2018.
 */
sealed class Read(val id: Int) {

    sealed class Content(id: Int) : Read(id) {

        class Book(id: Int, val name: String) : Content(id)

        class Chapter(val key: ChapterKey, val bookId: Int, val number: Int) : Content(key.id) {
            data class Key(val id: Int, val bookId: Int)
        }

    }

    class Verset(val key: VersetKey, val number: Int, val content: String, val isFavorite: Boolean) :
        Read(key.id) {
        data class Key(val id: Int, val bookId: Int, val chapterId: Int)
    }
}

class VersetProps(val key: VersetKey, val isFavorite: Boolean = false, val tags: List<String> = emptyList())

typealias VersetKey = Read.Verset.Key
typealias ChapterKey = Read.Content.Chapter.Key