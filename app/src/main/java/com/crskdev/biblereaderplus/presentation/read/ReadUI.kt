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

    fun getKey(): ReadKey = ReadKey(id)

    abstract fun setHasScrollPosition(value: Boolean): ReadUI

    abstract fun setIsBookmarked(value: Boolean): ReadUI

    abstract class ContentUI(id: Int, val name: String,
                             hasScrollPosition: HasScrollPosition,
                             isBookmarked: IsBookmarked,
                             val isExpanded: IsExpanded) :
        ReadUI(id, hasScrollPosition, isBookmarked) {

        abstract fun setExpanded(value: IsExpanded): ContentUI
    }

    class BookUI(
        id: Int,
        name: String,
        hasScrollPosition: HasScrollPosition = HasScrollPosition(false),
        isBookmarked: IsBookmarked = IsBookmarked(false),
        isExpanded: IsExpanded = IsExpanded(true)
    ) : ContentUI(id, name, hasScrollPosition, isBookmarked, isExpanded) {

        override fun setExpanded(value: IsExpanded): ContentUI =
            BookUI(id, name, hasScrollPosition, isBookmarked, value)

        override fun setHasScrollPosition(value: Boolean): ReadUI =
            BookUI(id, name, value.hasScrollPosition(), isBookmarked)

        override fun setIsBookmarked(value: Boolean) =
            BookUI(id, name, hasScrollPosition, value.isBookmarked())

    }

    class ChapterUI(
        id: Int,
        val bookId: Int,
        name: String,
        hasScrollPosition: HasScrollPosition = HasScrollPosition(false),
        isBookmarked: IsBookmarked = IsBookmarked(false),
        isExpanded: IsExpanded = IsExpanded(true)
    ) :
        ContentUI(id, name, hasScrollPosition, isBookmarked, isExpanded) {

        override fun setExpanded(value: IsExpanded): ContentUI =
            ChapterUI(id, bookId, name, hasScrollPosition, isBookmarked, value)

        override fun setHasScrollPosition(value: Boolean): ReadUI =
            ChapterUI(id, bookId, name, value.hasScrollPosition(), isBookmarked)

        override fun setIsBookmarked(value: Boolean) =
            ChapterUI(id, bookId, name, hasScrollPosition, value.isBookmarked())

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
            VersetUI(
                id,
                bookId,
                chapterId,
                number,
                contents,
                value.hasScrollPosition(),
                isBookmarked
            )

        override fun setIsBookmarked(value: Boolean) =
            VersetUI(
                id,
                bookId,
                chapterId,
                number,
                contents,
                hasScrollPosition,
                value.isBookmarked()
            )


    }
}

inline class ReadKey(val id: Int) {

    companion object {
        val INITIAL = ReadKey(0)
    }

    operator fun invoke() = id
}

inline class HasScrollPosition(val value: Boolean) {
    operator fun invoke() = value
}

inline class IsBookmarked(val value: Boolean) {
    operator fun invoke() = value
}

inline class IsExpanded(val value: Boolean) {
    operator fun invoke() = value
    operator fun not(): IsExpanded = IsExpanded(!value)
}

fun Boolean.isBookmarked(): IsBookmarked = IsBookmarked(this)
fun Boolean.hasScrollPosition(): HasScrollPosition = HasScrollPosition(this)