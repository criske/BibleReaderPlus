/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.entity

/**
 * Created by Cristian Pela on 07.11.2018.
 */
class Document(val books: List<Book>) {

    class Book(val name: String, val chapters: List<Chapter>, val id: Int? = null) : Read

    class Chapter(val number: Int, val versets: List<Verset>, val bookId: Int? = null) : Read

    class Verset(val number: Int, val bookId: Int? = null, val chapterId: Int? = null) : Read
}

interface Read