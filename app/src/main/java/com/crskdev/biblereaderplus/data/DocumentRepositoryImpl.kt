/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import com.crskdev.arch.coroutines.paging.dataSourceFactory
import com.crskdev.biblereaderplus.common.util.pagedlist.InMemoryPagedListDataSource
import com.crskdev.biblereaderplus.domain.entity.*
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.presentation.util.arch.filter
import com.crskdev.biblereaderplus.presentation.util.arch.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import java.util.*

/**
 * Created by Cristian Pela on 21.11.2018.
 */
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class DocumentRepositoryImpl : DocumentRepository {

    private data class Database(
        val versets: List<Read.Verset>,
        val tags: List<Tag>
    )

    private var dataSource: DataSource<Int, Read.Verset>? = null

    private val dbLiveData: MutableLiveData<Database>

    init {
        dbLiveData = MutableLiveData<Database>().apply {
            val versets = Random().let { r ->
                val wordLengths = listOf(5, 10, 3, 2, 7, 15, 12)
                (1..100)
                    .map { id ->
                        val paragraphLength = r.nextInt(10) + 50
                        var wordsCountDown = paragraphLength
                        val content = buildString {
                            while (wordsCountDown > 0) {
                                val delim = if (wordsCountDown > 0) " " else ""
                                val word = sequence {
                                    while (true) {
                                        yield((r.nextInt(24) + 97).toChar())
                                    }
                                }.take(wordLengths.random()).joinToString("") + delim
                                append(word)
                                wordsCountDown--
                            }
                        }
                        Read.Verset(
                            VersetKey(id, 1, 1, "remote$id"),
                            id,
                            content,
                            r.nextBoolean(),
                            ModifiedAt("")
                        )
                    }
            }

            val tags = (listOf(
                "#ffcc99",
                "#cc66ff",
                "#f7ffe6",
                "#80aaff",
                "#ff66a3",
                "#ffffff",
                "#000000"
            ) to listOf(
                "",
                "a",
                "abc",
                "abcd",
                "abdcdefg"
            )).let {
                val (colors, tagPrefixes) = it
                (0..100).map {
                    Tag(it + 1, "Tag${it + 1}${tagPrefixes.random()}", colors.random())
                }
            }
            value = Database(versets, tags)
        }
    }


    override fun save(reads: List<Read>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun read(reader: (PagedList<Read>) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun contents(): List<Read.Content> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun filter(query: String): List<Read.Content> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getVerset(versetKey: VersetKey): SelectedVerset? =
        dbLiveData.value?.versets?.firstOrNull { it.key == versetKey }?.let {
            SelectedVerset(
                it.key, "Book${it.key.bookId}", it.key.chapterId,
                it.key.id,
                it.content,
                it.isFavorite
            )
        }

    override suspend fun observeVerset(versetKey: VersetKey, observer: (SelectedVerset) -> Unit) =
        coroutineScope {
            val actor = actor<Read.Verset> {
                for (rv in channel) {
                    observer(rv.let {
                        SelectedVerset(
                            it.key, "Book${it.key.bookId}", it.key.chapterId,
                            it.key.id,
                            it.content,
                            it.isFavorite
                        )
                    })
                }
            }
            val liveDataObserver = Observer<List<Read.Verset>> {
                launch {
                    actor.send(it.first { it.key == versetKey })
                }
            }
            val versetsLiveData = dbLiveData
                .filter { it?.versets?.any { it.key == versetKey } ?: false }
                .map { it.versets }

            versetsLiveData.observeForever(liveDataObserver)

            actor.invokeOnClose {
                versetsLiveData.removeObserver(liveDataObserver)
            }
            Unit
        }

    override fun getVersetProps(versetKey: VersetKey): VersetProps {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun synchronize() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun favoriteAction(versetKey: VersetKey, add: Boolean) {
        dbLiveData.postValue(dbLiveData.value?.let {
            it.copy(versets = it.versets.map {
                if (it.key == versetKey) {
                    it.copy(isFavorite = add)
                } else {
                    it
                }
            })
        })
        sleep(200)//gib time for da value to settle
        dataSource?.invalidate()
    }

    @Synchronized
    override fun favorites(filter: FavoriteFilter): DataSource.Factory<Int, Read.Verset> {
        dbLiveData.value = dbLiveData.value?.let {
            it.copy(versets = it.versets.map {
                it.copy(content = "$filter|->:${it.content}")
            })
        }
        return dataSourceFactory {
            InMemoryPagedListDataSource {
                dbLiveData.value?.versets ?: emptyList()
            }.apply {
                dataSource = this
            }
        }
    }

}