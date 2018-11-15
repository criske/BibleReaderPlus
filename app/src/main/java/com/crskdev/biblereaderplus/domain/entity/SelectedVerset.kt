package com.crskdev.biblereaderplus.domain.entity

data class SelectedVerset(val key: VersetKey,
                          val bookName: String,
                          val chapterNumber: Int,
                          val number: Int,
                          val contents: String,
                          val isFavorite: Boolean = false,
                          val tags: List<String> = emptyList())
