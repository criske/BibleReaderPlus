/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import com.crskdev.arch.coroutines.paging.dataSourceFactory
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.common.util.pagedlist.InMemoryPagedListDataSource
import com.crskdev.biblereaderplus.domain.entity.*
import com.crskdev.biblereaderplus.domain.gateway.DateFormatter
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
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by Cristian Pela on 21.11.2018.
 */

val MOCKED_DB: DocumentRepositoryImpl.Database  by lazy {

    val r = Random()
    val wordLengths = listOf(5, 10, 3, 2, 7, 15, 12)
    fun generateWord(random: Random, len: Int, atLeastLen: Int = 2): String {
        return sequence {
            while (true) {
                yield((random.nextInt(24) + 97).toChar())
            }
        }.take(len.coerceAtLeast(atLeastLen)).joinToString("")
    }


    val numberSeed = 3

    val books = mutableListOf<Read.Content.Book>()
    val chapters = mutableListOf<Read.Content.Chapter>()
    val versets = mutableListOf<Read.Verset>()
    var idGen = -0

    val modifiedAt = ModifiedAt(DateFormatter().getDateString())

    for (i in 1..numberSeed) {
        val bookName = generateWord(r, wordLengths.random(), 5)
        val book = Read.Content.Book(idGen++, bookName, bookName.substring(0, 2), modifiedAt)
        books.add(book)
        for (j in 1..r.nextInt(numberSeed) + 5) {
            val chapter =
                Read.Content.Chapter(ChapterKey(idGen++, book.id), j, book.name, modifiedAt)
            chapters.add(chapter)
            for (k in 1..r.nextInt(numberSeed) + 10) {
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
                        VersetKey(idGen++, book.id, chapter.id, "remote$idGen"),
                        k,
                        book.name,
                        chapter.number,
                        content,
                        //r.nextBoolean(),
                        false,
                        modifiedAt
                    )
                )
            }
        }
    }
    val tags =
            /*(listOf(
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
            }*/ emptyList<Tag>()

    DocumentRepositoryImpl.Database(
        books,
        chapters,
        versets,
        tags.toSet(),
        /*setOf(
            DocumentRepositoryImpl.VersetTag(
                versets.first { it.isFavorite }.key.id,
                tags.first().id
            ),
            DocumentRepositoryImpl.VersetTag(
                versets.first { it.isFavorite }.key.id,
                tags[1].id
            )
        )*/ emptySet()
    )
}


@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class DocumentRepositoryImpl : DocumentRepository {

    data class Database(
        val books: List<Read.Content.Book>,
        val chapters: List<Read.Content.Chapter>,
        val versets: List<Read.Verset>,
        val tags: Set<Tag>,
        val versetTags: Set<VersetTag>) {
        fun allReads(): List<Read> = (books + chapters + versets).sortedBy { it.id }
    }

    data class VersetTag(val versetId: Int, val tagId: String)

    private val dataSourceManager = DataSourceManager()

    private val dbLiveData: MutableLiveData<Database>

    companion object {
        private const val KEY_DS_READ = 0
        private const val KEY_DS_FAVORITE_VERSETS = 1
        val EMPTY = Database(emptyList(), emptyList(), emptyList(), emptySet(), emptySet())
    }

    init {
        dbLiveData = MutableLiveData<Database>().apply {
            value = EMPTY
        }
    }

    override fun runTransaction(block: DocumentRepository.() -> Unit) {
        block()
    }

    override fun save(reads: List<Read>) {
        updateDatabasePost {
            val books = mutableListOf<Read.Content.Book>()
            val chapters = mutableListOf<Read.Content.Chapter>()
            val versets = mutableListOf<Read.Verset>()
            reads.forEach {
                when (it) {
                    is Read.Content.Book -> books.add(it)
                    is Read.Content.Chapter -> chapters.add(it)
                    is Read.Verset -> versets.add(it)
                }
            }
            copy(books = books, chapters = chapters, versets = versets)

        }
    }

    override fun read(): DataSource.Factory<Int, Read> =
        dataSourceFactory {
            InMemoryPagedListDataSource {
                dbLiveData.value?.allReads() ?: emptyList()
            }.apply {
                dataSourceManager.addOrReplace(KEY_DS_READ, this)
            }
        }

    override fun contents(): List<Read.Content> =
        dbLiveData.value
            ?.allReads()
            ?.filter { it is Read.Content }
            ?.map { it as Read.Content }
            ?: emptyList()

    override fun filterContents(contains: String): List<Read.Content> {
        val originals = contents()
        val bookCandidates = originals
            .filter { it is Read.Content.Book && it.name.contains(contains, true) }
            .map { it.cast<Read.Content.Book>() }
        return originals.filter { read ->
            if (read is Read.Content.Chapter) {
                bookCandidates.any { read.cast<Read.Content.Chapter>().key.bookId == it.id }
            } else
                bookCandidates.contains(read)
        }
    }


    override fun favoriteAction(add: Boolean, id: Int, modifiedAt: ModifiedAt) {
        updateDatabasePost {
            copy(
                versets = versets.map {
                    if (it.key.id == id) {
                        it.copy(isFavorite = add, modifiedAt = modifiedAt)
                    } else {
                        it
                    }
                },
                versetTags = if (!add) versetTags.filter { it.versetId != id }.toSet() else versetTags
            )
        }
    }

    override fun favoriteActionBatch(add: Boolean, vararg ids: Pair<Int, ModifiedAt>) {
        updateDatabasePost {
            copy(
                versets = versets.map { rv ->
                    val contains = ids.firstOrNull { it.first == rv.id }
                    if (contains != null) {
                        rv.copy(isFavorite = add, modifiedAt = contains.second)
                    } else {
                        rv
                    }
                },
                versetTags = if (!add) versetTags.filter { !ids.map { it.first }.contains(it.versetId) }.toSet() else versetTags
            )
        }
    }

    override fun getVerset(id: Int): SelectedVerset? =
        dbLiveData.value?.let { db ->
            db.versets.firstOrNull { it.key.id == id }?.let { v ->
                SelectedVerset(
                    v.key,
                    v.bookName,
                    v.chapterNumber,
                    v.number,
                    v.content,
                    v.isFavorite,
                    db.versetTags.filter { it.versetId == id }
                        .map { t -> db.tags.first { it.id == t.tagId } }
                )
            }
        }

    override suspend fun observeVerset(id: Int, observer: (SelectedVerset) -> Unit) =
        coroutineScope {
            val actor = actor<SelectedVerset> {
                for (rv in channel) {
                    observer(rv)
                }

            }
            val liveDataObserver = Observer<Database> { db ->
                launch {
                    val v = db.versets.first { it.key.id == id }
                    val tags = db.versetTags.filter { it.versetId == id }
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
                .filter { it?.versets?.any { it.key.id == id } ?: false }
                .observeForever(liveDataObserver)

            actor.invokeOnClose {
                dbLiveData.removeObserver(liveDataObserver)
            }
            Unit
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

    override fun tagFavoriteVerset(add: Boolean, versetId: Int, tagId: String, modifiedAt: ModifiedAt) {
        updateDatabasePost {
            val vt = VersetTag(versetId, tagId)
            val maybeUpdateFavDb =
                if (add && versets.any { it.id == versetId && !it.isFavorite }) { // add verset to favorites first if wasn't prior added
                    copy(
                        versets = versets.map {
                            if (it.key.id == versetId) {
                                it.copy(isFavorite = add, modifiedAt = modifiedAt)
                            } else {
                                it
                            }
                        })
                } else
                    this
            maybeUpdateFavDb.copy(versetTags = if (add) versetTags + vt else versetTags - vt)
        }
    }

    override fun tagFavoriteVersetBatch(add: Boolean, versetId: Int, modifiedAt: ModifiedAt, vararg tagIds: String) {
        updateDatabasePost {
            var updateVersets = this.versets
            var updateVersetTags = this.versetTags
            tagIds.forEach { t ->
                val vt = VersetTag(versetId, t)
                if (add) {
                    if (updateVersets.any { it.id == versetId && !it.isFavorite }) {
                        updateVersets = updateVersets.map {
                            if (it.key.id == versetId) {
                                it.copy(isFavorite = add, modifiedAt = modifiedAt)
                            } else {
                                it
                            }
                        }
                    }
                    updateVersetTags += vt
                } else {
                    updateVersetTags -= vt
                }
            }
            copy(versets = updateVersets, versetTags = updateVersetTags)
        }
    }

    override fun tagCreate(vararg newTags: Tag) {
        updateDatabasePost {
            copy(tags = tags + newTags)
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
                                db.versetTags.any { vt -> vt.versetId == v.key.id && f.any { it.id == vt.tagId } }
                            } ?: true
                        }
                } ?: emptyList()
            }.apply {
                dataSourceManager.addOrReplace(KEY_DS_FAVORITE_VERSETS, this)
            }
        }
    }


    //###############################################################################################

    private inline fun updateDatabasePost(block: Database.() -> Database) {
        dbLiveData.postValue(dbLiveData.value?.block())
        sleep(200)//gib time for da value to settle
        dataSourceManager.invalidate()
    }

}

class DataSourceManager {

    private val dataSources = ConcurrentHashMap<Int, DataSource<Int, *>>()

    fun addOrReplace(key: Int, dataSource: DataSource<Int, *>) {
        dataSources.put(key, dataSource)
    }


    fun invalidate() {
        dataSources.values.forEach {
            it.invalidate()
        }
    }
}