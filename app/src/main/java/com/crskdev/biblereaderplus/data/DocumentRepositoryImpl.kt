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
        val books: List<Read.Content.Book>,
        val chapters: List<Read.Content.Chapter>,
        val versets: List<Read.Verset>,
        val tags: Set<Tag>,
        val versetTags: Set<VersetTag>
    )

    private data class VersetTag(val versetKey: VersetKey, val tagId: String)

    private var dataSource: DataSource<Int, Read.Verset>? = null

    private val dbLiveData: MutableLiveData<Database>

    init {
        dbLiveData = MutableLiveData<Database>().apply {

            val r = Random()
            val wordLengths = listOf(5, 10, 3, 2, 7, 15, 12)
            fun generateWord(random: Random, len: Int, atLeastLen: Int = 2): String {
                return sequence {
                    while (true) {
                        yield((random.nextInt(24) + 97).toChar())
                    }
                }.take(len.coerceAtLeast(atLeastLen)).joinToString("")
            }


            val books = mutableListOf<Read.Content.Book>()
            val chapters = mutableListOf<Read.Content.Chapter>()
            val versets = mutableListOf<Read.Verset>()
            var chapterId = 0
            var versetId = 0
            for (bookId in 1..20) {
                val book = Read.Content.Book(bookId, generateWord(r, wordLengths.random(), 5))
                books.add(book)
                for (c in 5..r.nextInt(20) + 5) {
                    val chapter = Read.Content.Chapter(ChapterKey(chapterId++, bookId), c)
                    chapters.add(chapter)
                    for (v in 10..r.nextInt(20) + 10) {
                        val paragraphLength = r.nextInt(10) + 50
                        var wordsCountDown = paragraphLength
                        val content = buildString {
                            while (wordsCountDown > 0) {
                                val delim = if (wordsCountDown > 0) " " else ""
                                val word = generateWord(r, wordLengths.random()) + delim
                                append(word)
                                wordsCountDown--
                            }
                        }
                        versets.add(
                            Read.Verset(
                                VersetKey(versetId++, book.id, chapter.id, "remote$versetId"),
                                v,
                                book.name,
                                chapter.number,
                                content,
                                r.nextBoolean(),
                                ModifiedAt("")
                            )
                        )
                    }
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
                books,
                chapters,
                versets, tags.toSet(), setOf(
                    VersetTag(versets.first { it.isFavorite }.key, tags.first().id),
                    VersetTag(versets.first { it.isFavorite }.key, tags[1].id)
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
            copy(
                versets = versets.map {
                    if (it.key == versetKey) {
                        it.copy(isFavorite = add)
                    } else {
                        it
                    }
                },
                versetTags = if (!add) versetTags.filter {
                    it.versetKey != versetKey
                }.toSet() else versetTags
            )
        }
    }

    override fun getVerset(versetKey: VersetKey): SelectedVerset? =
        dbLiveData.value?.let { db ->
            db.versets.firstOrNull { it.key == versetKey }?.let { v ->
                SelectedVerset(
                    v.key,
                    v.bookName,
                    v.chapterNumber,
                    v.number,
                    v.content,
                    v.isFavorite,
                    db.versetTags.filter { it.versetKey == versetKey }
                        .map { t -> db.tags.first { it.id == t.tagId } }
                )
            }
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
                    val tags = db.versetTags.filter { it.versetKey == versetKey }
                        .map { t -> db.tags.first { it.id == t.tagId } }
                    val selected = SelectedVerset(
                        v.key,
                        db.books.first { it.id == v.key.bookId }.name,
                        db.chapters.first { it.id == v.key.chapterId }.number,
                        v.number,
                        v.content,
                        v.isFavorite,
                        tags
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
        updateDatabasePost {
            copy(tags = tags + newTag)
        }
    }

    override fun tagDelete(id: String) {
        updateDatabasePost {
            copy(
                tags = tags.filter { it.id != id }.toSet(),
                versetTags = versetTags.filter { it.tagId != id }.toSet()
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
                        .filter { it.isFavorite }
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