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

    abstract val id: Int

    abstract val hasScrollPosition: HasScrollPosition

    abstract val isBookmarked: IsBookmarked

    fun getKey(): ReadKey = ReadKey(id)

    abstract fun setHasScrollPosition(value: Boolean): ReadUI

    abstract fun setIsBookmarked(value: Boolean): ReadUI

    abstract class ContentUI : ReadUI() {

        abstract val name: String

        abstract val isExpanded: IsExpanded

        abstract fun setExpanded(value: IsExpanded): ContentUI
    }

    data class BookUI(
        override val id: Int,
        override val name: String,
        override val hasScrollPosition: HasScrollPosition = HasScrollPosition(false),
        override val isBookmarked: IsBookmarked = IsBookmarked(false),
        override val isExpanded: IsExpanded = IsExpanded(true)
    ) : ContentUI() {

        override fun setExpanded(value: IsExpanded): ContentUI =
            BookUI(id, name, hasScrollPosition, isBookmarked, value)

        override fun setHasScrollPosition(value: Boolean): ReadUI =
            BookUI(id, name, value.hasScrollPosition(), isBookmarked)

        override fun setIsBookmarked(value: Boolean) =
            BookUI(id, name, hasScrollPosition, value.isBookmarked())

    }

    data class ChapterUI(
        override val id: Int,
        val bookId: Int,
        override val name: String,
        override val hasScrollPosition: HasScrollPosition = HasScrollPosition(false),
        override val isBookmarked: IsBookmarked = IsBookmarked(false),
        override val isExpanded: IsExpanded = IsExpanded(true)
    ) : ContentUI() {

        override fun setExpanded(value: IsExpanded): ContentUI =
            ChapterUI(id, bookId, name, hasScrollPosition, isBookmarked, value)

        override fun setHasScrollPosition(value: Boolean): ReadUI =
            ChapterUI(id, bookId, name, value.hasScrollPosition(), isBookmarked)

        override fun setIsBookmarked(value: Boolean) =
            ChapterUI(id, bookId, name, hasScrollPosition, value.isBookmarked())

    }

    data class VersetUI(
        override val id: Int,
        val bookId: Int,
        val chapterId: Int,
        val number: Int,
        val contents: CharSequence,
        override val hasScrollPosition: HasScrollPosition = HasScrollPosition(false),
        override val isBookmarked: IsBookmarked = IsBookmarked(false)
    ) : ReadUI() {
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