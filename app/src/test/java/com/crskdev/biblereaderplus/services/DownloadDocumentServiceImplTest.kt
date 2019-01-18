/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.services

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.junit.Test

/**
 * Created by Cristian Pela on 17.01.2019.
 */
class DownloadDocumentServiceImplTest {

    @Test
    fun parse() {
        val json = """
            [
                {
                    "abbrev": "gn",
                    "chapters": [
                            [
                                "La început, Dumnezeu a făcut cerurile şi pămîntul.",
                                "Pămîntul era pustiu şi gol; peste faţa adîncului de ape era întunerec, şi Duhul lui Dumnezeu se mişca pe deasupra apelor.", "Dumnezeu a zis: ,,Să fie lumină!`` Şi a fost lumină."
                            ],
                            ["v1","v2"],
                            ["v1","v2"]
                    ],
                    "name":"Genesis"
                }
            ]
        """.trimIndent()

        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, Book::class.java)
        val adapter = moshi.adapter<List<Book>>(type)


        val bible = adapter.fromJson(json)

        println(bible)
    }


    class Bible {
        lateinit var books: List<Book>
    }

    class Book {
        lateinit var abbrev: String
        lateinit var chapters: List<List<String>>
        lateinit var name: String
    }
}