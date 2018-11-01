/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.crskdev.biblereaderplus.presentation.read

/**
 * Created by Cristian Pela on 31.10.2018.
 */
sealed class ReadUI {

    abstract fun getKey(): ReadKey

    class BookUI(val id: Int, val name: String) : ReadUI() {
        override fun getKey(): ReadKey = ReadKey("bk$id")
    }

    class ChapterUI(val id: Int, val bookId: Int, val name: String) : ReadUI() {
        override fun getKey(): ReadKey = ReadKey("bk$bookId-ch$id")
    }

    class VersetUI(val id: Int, val bookId: Int, val chapterId: Int, number: Int, val contents: CharSequence) :
        ReadUI() {
        override fun getKey(): ReadKey = ReadKey("bk$bookId-ch$id-v$id")
    }
}

inline class ReadKey(val key: String)