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
import com.crskdev.biblereaderplus.presentation.util.arch.toChannel
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
        val tags: Set<Tag>,
        val versetTags: Set<VersetTag>
    )

    private data class VersetTag(val versetKey: VersetKey, val tagId: String)

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
                    Tag((it + 1).toString(), "Tag${it + 1}${tagPrefixes.random()}", colors.random())
                }
            }
            value = Database(
                versets, tags.toSet(), setOf(
                    VersetTag(versets.first().key, tags.first().id),
                    VersetTag(versets.first().key, tags[1].id),
                    VersetTag(versets[1].key, tags.first().id)
                )
            )
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

    override fun filter(contains: String): List<Read.Content> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun favoriteAction(versetKey: VersetKey, add: Boolean) {
        updateDatabasePost {
            copy(versets = versets.map {
                if (it.key == versetKey) {
                    it.copy(isFavorite = add)
                } else {
                    it
                }
            })
        }
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
            val actor = actor<SelectedVerset> {
                for (rv in channel) {
                    observer(rv)
                }

            }
            val liveDataObserver = Observer<Database> { db ->
                launch {
                    val v = db.versets.first { it.key == versetKey }
                    val vts = db.versetTags.filter { it.versetKey == versetKey }
                        .map { t -> db.tags.first { it.id == t.tagId } }
                    val selected = SelectedVerset(
                        v.key, "Book${v.key.bookId}", v.key.chapterId,
                        v.key.id,
                        v.content,
                        v.isFavorite,
                        vts
                    )
                    actor.send(selected)
                }
            }
            dbLiveData
                .filter { it?.versets?.any { it.key == versetKey } ?: false }
                .observeForever(liveDataObserver)

            actor.invokeOnClose {
                dbLiveData.removeObserver(liveDataObserver)
            }
            Unit
        }

    override fun getVersetProps(versetKey: VersetKey): VersetProps {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun synchronize() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //#####################################TAG OPERATIONS###########################################

    override suspend fun tagsObserve(contains: String?, observer: (Set<Tag>) -> Unit) =
        coroutineScope {
            dbLiveData.toChannel {
                for (db in it) {
                    val tags = db.tags
                    val filtered = (contains?.let { c ->
                        tags.filter { it.name.contains(c, true) }.toSet()
                    } ?: tags)
                    observer(filtered)
                }
            }
            Unit
        }

    override fun tagFavoriteVerset(versetKey: VersetKey, tagId: String, add: Boolean) {
        updateDatabasePost {
            val vt = VersetTag(versetKey, tagId)
            copy(versetTags = if (add) versetTags + vt else versetTags - vt)
        }
    }

    override fun tagCreate(newTag: Tag) {
//        updateDatabasePost {
//            copy(tags = tags + newTag)
//        }
        TODO("Oops! Need to implement tag creation")
    }

    override fun tagDelete(id: String) {
        updateDatabasePost {
            copy(
                tags = tags.filter { it.id != id }.toSet(),
                versetTags = versetTags.filter { it.tagId == id }.toSet()
            )
        }
    }

    override fun tagRename(id: String, newName: String) {
        updateDatabasePost {
            copy(
                tags = tags.map { if (id == it.id) it.copy(name = newName) else it }.toSet()
            )
        }
    }

    override fun tagColor(id: String, color: String) {
        updateDatabasePost {
            copy(
                tags = tags.map { if (id == it.id) it.copy(color = color) else it }.toSet()
            )
        }
    }

    @Synchronized
    override fun favorites(filter: FavoriteFilter): DataSource.Factory<Int, Read.Verset> {
        return dataSourceFactory {
            InMemoryPagedListDataSource {
                dbLiveData.value?.let { db ->
                    db.versets
                        .filter { v ->
                            filter.query?.let { v.content.contains(it, true) } ?: true
                        }
                        .filter { v ->
                            filter.tags.takeIf { it.isNotEmpty() }?.let { f ->
                                db.versetTags.any { vt -> vt.versetKey == v.key && f.any { it.id == vt.tagId } }
                            } ?: true
                        }
                } ?: emptyList()
            }.apply {
                dataSource = this
            }
        }
    }


    //###############################################################################################

    private inline fun updateDatabasePost(block: Database.() -> Database) {
        dbLiveData.postValue(dbLiveData.value?.block())
        sleep(200)//gib time for da value to settle
        dataSource?.invalidate()
    }

}