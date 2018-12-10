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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.Thread.sleep
import java.util.*

/**
 * Created by Cristian Pela on 21.11.2018.
 */
class DocumentRepositoryImpl : DocumentRepository {

    private var dataSource: DataSource<Int, Read.Verset>? = null

    private val versetsLiveData = MutableLiveData<List<Read.Verset>>().apply {
        value = Random().let { r ->
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
        versetsLiveData.value?.firstOrNull { it.key == versetKey }?.let {
            SelectedVerset(
                it.key, "Book${it.key.bookId}", it.key.chapterId,
                it.key.id,
                it.content,
                it.isFavorite
            )
        }

    override suspend fun observeVerset(versetKey: VersetKey, observer: (SelectedVerset) -> Unit) = coroutineScope {
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
            launch(Dispatchers.Default) {
                actor.send(it.first { it.key == versetKey })
            }
        }
        versetsLiveData
            .filter { it?.any { it.key == versetKey } ?: false }
            .observeForever(liveDataObserver)

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
        versetsLiveData.postValue(versetsLiveData.value?.map {
            if (it.key == versetKey) {
                it.copy(isFavorite = add)
            } else {
                it
            }
        })
        sleep(200)//gib time for da value to settle
        dataSource?.invalidate()
    }

    @Synchronized
    override fun favorites(filter: FavoriteFilter): DataSource.Factory<Int, Read.Verset> {
        versetsLiveData.value = versetsLiveData.value?.map {
            it.copy(content = "$filter|->:${it.content}")
        }
        return dataSourceFactory {
            InMemoryPagedListDataSource {
                versetsLiveData.value ?: emptyList()
            }.apply {
                dataSource = this
            }
        }
    }

}