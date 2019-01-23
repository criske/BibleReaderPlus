/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

@file:Suppress("unused")

package com.crskdev.biblereaderplus.data

import android.content.Context
import androidx.paging.DataSource
import androidx.room.Room
import com.crskdev.biblereaderplus.data.internal.room.*
import com.crskdev.biblereaderplus.domain.entity.*
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import com.crskdev.biblereaderplus.presentation.util.arch.toChannel
import kotlinx.coroutines.coroutineScope

/**
 * Created by Cristian Pela on 21.01.2019.
 */
class RoomDocumentRepositoryImpl(context: Context) : DocumentRepository {

    private val db by lazy {
        //Room.databaseBuilder(context, DocumentDatabase::class.java, "bible-db").build()
        Room.inMemoryDatabaseBuilder(context, DocumentDatabase::class.java).build()
    }

    private val readDao by lazy {
        db.readDAO()
    }

    private val tagDao by lazy {
        db.tagDAO()
    }

    override fun runTransaction(block: DocumentRepository.() -> Unit) {
        if (db.inTransaction()) {
            block()
        } else {
            db.beginTransaction()
            try {
                this.block()
                db.setTransactionSuccessful()
            } catch (ex: Exception) {
                throw ex
            } finally {
                db.endTransaction()
            }
        }
    }

    override fun save(reads: List<Read>) {
        readDao.insert(reads.map { it.toDb() })
    }

    override fun read(): DataSource.Factory<Int, Read> =
        readDao.observeReads().mapByPage { p -> p.map { it.toDomain() } }

    override fun contents(): List<Read.Content> =
        readDao.contents().map { it.toDomain() as Read.Content }

    override fun filterContents(contains: String): List<Read.Content> =
        readDao.contents("%contains%").map { it.toDomain() as Read.Content }

    override fun getVerset(id: Int): SelectedVerset? =
        readDao.getVerset(id)?.toSelectedVerset()

    override suspend fun observeVerset(id: Int, observer: (SelectedVerset) -> Unit) {
        coroutineScope {
            readDao.observeVerset(id).toChannel {
                for (vdb in it) {
                    val v = vdb
                    observer(vdb.toSelectedVerset())
                }
            }
        }
    }

    override fun favoriteAction(add: Boolean, id: Int, modifiedAt: ModifiedAt) {
        runTransaction {
            try {
                if (!add) {
                    tagDao.removeAllTagsFromFavorites(id)
                }
                readDao.getRead(id).apply {
                    this.isFavorite = add
                    this.modifiedAt = modifiedAt.date
                }.let {
                    readDao.updateRead(it)
                }
                // readDao.updateFavorite(id, add, modifiedAt.date)
            } catch (x: java.lang.Exception) {
                println(x)
            }
        }
    }

    override fun favoriteActionBatch(add: Boolean, vararg ids: Pair<Int, ModifiedAt>) {
        runTransaction {
            for (pair in ids) {
                if (!add) {
                    tagDao.removeAllTagsFromFavorites(pair.first)
                }
                readDao.updateFavorite(pair.first, add, pair.second.date)
            }
        }
    }

    override fun favorites(filter: FavoriteFilter): DataSource.Factory<Int, Read.Verset> =
    //TODO filter use cases
        readDao.observeReadsFavorite().mapByPage { p -> p.map { it.toDomain() as Read.Verset } }

    override suspend fun tagsObserve(contains: String?, observer: (Set<Tag>) -> Unit) =
        coroutineScope {
            tagDao.observeTags().toChannel { ch ->
                for (tags in ch) {
                    observer(tags.map { it.toDomain() }.toSet())
                }
            }
        }

    override fun tagFavoriteVerset(add: Boolean, versetId: Int, tagId: String, modifiedAt: ModifiedAt) {
        tagFavoriteVersetBatch(add, versetId, modifiedAt, tagId)
    }

    override fun tagFavoriteVersetBatch(add: Boolean, versetId: Int, modifiedAt: ModifiedAt, vararg tagIds: String) {
        val list = tagIds.map {
            VersetTagDB().apply {
                this.versetId = versetId
                this.tagId = it
            }
        }
        runTransaction {
            favoriteAction(true, versetId, modifiedAt)
            if (add) {
                tagDao.addTagToFavorites(list)
            } else {
                for (id in tagIds) {
                    tagDao.removeTagFromFavorites(id, versetId)
                }
            }
        }

    }

    override fun tagDelete(id: String) {
        tagDao.delete(id)
    }

    override fun tagRename(id: String, newName: String) {
        runTransaction {
            tagDao.getTag(id).apply { name = newName }.let {
                tagDao.update(it)
            }
        }
    }

    override fun tagColor(id: String, color: String) {
        runTransaction {
            tagDao.getTag(id).apply { this.color = color }.let {
                tagDao.update(it)
            }
        }
    }

    override fun tagCreate(vararg newTags: Tag) {
        tagDao.insert(newTags.map { it.toDb() })
    }


    //*****************************MAPPERS****************************
    private fun Read.toDb() = ReadDB().apply {
        val r = this@toDb
        id = r.id
        modifiedAt = r.modifiedAt.date
        when (r) {
            is Read.Content.Book -> {
                type = ReadDB.TYPE_BOOK
                abbreviation = r.abbreviation
                name = r.name
            }
            is Read.Content.Chapter -> {
                type = ReadDB.TYPE_CHAPTER
                bookId = r.key.bookId
                number = r.number
                chapterNumber = r.number
                name = r.bookName
            }
            is Read.Verset -> {
                type = ReadDB.TYPE_VERSET
                bookId = r.key.bookId
                chapterId = r.key.chapterId
                number = r.number
                chapterNumber = r.chapterNumber
                abbreviation = r.bookName
                content = r.content.toString()
            }
        }


    }

    private fun ReadDB.toDomain(): Read =
        when (type) {
            ReadDB.TYPE_BOOK -> Read.Content.Book(
                id,
                name ?: "",
                abbreviation ?: "",
                ModifiedAt(modifiedAt)
            )
            ReadDB.TYPE_CHAPTER -> Read.Content.Chapter(
                ChapterKey(id, bookId),
                number,
                name ?: "",
                ModifiedAt(modifiedAt)
            )
            ReadDB.TYPE_VERSET -> Read.Verset(
                VersetKey(id, bookId, chapterId, ""), number
                , abbreviation ?: "", chapterNumber, content ?: "", isFavorite,
                ModifiedAt(modifiedAt)
            )
            else -> throw Exception("Unknown read db type")
        }

    private fun VersetDB.toSelectedVerset(): SelectedVerset =
        SelectedVerset(
            VersetKey(id, bookId, chapterId, ""),
            abbreviation ?: "",
            chapterNumber,
            number,
            content,
            isFavorite,
            tags?.map { Tag(it.id, it.name, it.color) } ?: emptyList()
        )

    private fun TagDB.toDomain(): Tag = Tag(id, name, color)

    private fun Tag.toDb() = TagDB().apply {
        id = this@toDb.id
        name = this@toDb.name
        color = this@toDb.color
    }
}