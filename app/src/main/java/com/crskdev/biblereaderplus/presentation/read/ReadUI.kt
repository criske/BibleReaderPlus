/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.crskdev.biblereaderplus.presentation.read

/**
 * Created by Cristian Pela on 31.10.2018.
 */
sealed class ReadUI(val id: Int, val hasScrollPosition: HasScrollPosition, val isBookmarked: IsBookmarked) {

    abstract fun getKey(): ReadKey

    abstract fun setHasScrollPosition(value: Boolean): ReadUI
    abstract fun setIsBookmarked(value: Boolean): ReadUI

    class BookUI(
        id: Int,
        val name: String,
        hasScrollPosition: HasScrollPosition = HasScrollPosition(false),
        isBookmarked: IsBookmarked = IsBookmarked(false)
    ) :
        ReadUI(id, hasScrollPosition, isBookmarked) {

        override fun setHasScrollPosition(value: Boolean): ReadUI =
            BookUI(id, name, value.hasScrollPosition(), isBookmarked)

        override fun setIsBookmarked(value: Boolean) = BookUI(id, name, hasScrollPosition, value.isBookmarked())

        override fun getKey(): ReadKey = ReadKey("$id")
    }

    class ChapterUI(
        id: Int,
        val bookId: Int,
        val name: String,
        hasScrollPosition: HasScrollPosition = HasScrollPosition(false),
        isBookmarked: IsBookmarked = IsBookmarked(false)
    ) :
        ReadUI(id, hasScrollPosition, isBookmarked) {
        override fun setHasScrollPosition(value: Boolean): ReadUI =
            ChapterUI(id, bookId, name, value.hasScrollPosition(), isBookmarked)

        override fun setIsBookmarked(value: Boolean) =
            ChapterUI(id, bookId, name, hasScrollPosition, value.isBookmarked())

        override fun getKey(): ReadKey = ReadKey("$bookId-$id")
    }

    class VersetUI(
        id: Int,
        val bookId: Int,
        val chapterId: Int,
        val number: Int,
        val contents: CharSequence,
        hasScrollPosition: HasScrollPosition = HasScrollPosition(false),
        isBookmarked: IsBookmarked = IsBookmarked(false)
    ) :
        ReadUI(id, hasScrollPosition, isBookmarked) {
        override fun setHasScrollPosition(value: Boolean): ReadUI =
            VersetUI(id, bookId, chapterId, number, contents, value.hasScrollPosition(), isBookmarked)

        override fun setIsBookmarked(value: Boolean) =
            VersetUI(id, bookId, chapterId, number, contents, hasScrollPosition, value.isBookmarked())

        override fun getKey(): ReadKey = ReadKey("$bookId-$chapterId-$id")
    }
}

inline class ReadKey(val value: String) {

    companion object {
        val INITIAL = ReadKey("0")
    }

    operator fun invoke() = value
    operator fun component1(): Int = value.split("-").firstOrNull()?.toInt() ?: -1
    operator fun component2(): Int = value.split("-").getOrNull(1)?.toInt() ?: -1
    operator fun component3(): Int = value.split("-").getOrNull(2)?.toInt() ?: -1
}

inline class HasScrollPosition(val value: Boolean) {
    operator fun invoke() = value
}

inline class IsBookmarked(val value: Boolean) {
    operator fun invoke() = value
}

fun Boolean.isBookmarked(): IsBookmarked = IsBookmarked(this)
fun Boolean.hasScrollPosition(): HasScrollPosition = HasScrollPosition(this)