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

        class Chapter(id: Int, val bookId: Int, val number: Int) : Content(id)

    }

    class Verset(id: Int, val bookId: Int, val chapterId: Int, val number: Int, val content: String) :
        Read(id)
}