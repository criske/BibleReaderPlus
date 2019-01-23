/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.services

import com.crskdev.biblereaderplus.domain.entity.ChapterKey
import com.crskdev.biblereaderplus.domain.entity.ModifiedAt
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.gateway.DateFormatter
import com.crskdev.biblereaderplus.domain.gateway.DownloadDocumentService
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageException.*
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.ByteString
import okio.Okio
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Cristian Pela on 07.01.2019.
 */
class DownloadDocumentServiceImpl(private val dateFormatter: DateFormatter) :
    DownloadDocumentService {

    private val idGenerator: AtomicInteger = AtomicInteger()

    private val firebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val jsonAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, BookJSON::class.java)
        Moshi.Builder()
            .build()
            .adapter<List<BookJSON>>(type)
    }

    override fun download(): DownloadDocumentService.Response {

        val storeRef = firebaseStorage
            .getReferenceFromUrl("gs://biblereaderplus.appspot.com/")
            .child("ro_cornilescu.json")

        return try {
            val jsonInputStream = Tasks.await(storeRef.stream).stream
            val jsonSource = Okio.buffer(Okio.source(jsonInputStream)).apply {
                val utf8BOM = ByteString.decodeHex("EFBBBF")
                if (rangeEquals(0, utf8BOM)) {
                    skip(utf8BOM.size().toLong())
                }
            }
            val jsonBible = jsonAdapter.fromJson(
                JsonReader
                    .of(jsonSource)
                    .apply { isLenient = true })
                ?: emptyList()
            DownloadDocumentService.Response.OKResponse(response(jsonBible))
        } catch (ex: Exception) {
            val cause = ex.cause ?: ex
            when (cause) {
                is StorageException -> {
                    val msg = when (cause.errorCode) {
                        ERROR_OBJECT_NOT_FOUND -> "No object exists at the desired reference."
                        ERROR_BUCKET_NOT_FOUND -> "No bucket is configured for Cloud Storage"
                        ERROR_PROJECT_NOT_FOUND -> "No project is configured for Cloud Storage"
                        ERROR_QUOTA_EXCEEDED -> "Quota on your Cloud Storage bucket has been exceeded.If you 're on the free tier, upgrade to a paid plan. If you' re on a paid plan, reach out to Firebase support."

                        ERROR_NOT_AUTHENTICATED -> "User is unauthenticated), please authenticate and try again."
                        ERROR_NOT_AUTHORIZED -> "User is not authorized to perform the desired action, check your rules to ensure they are correct."
                        ERROR_RETRY_LIMIT_EXCEEDED -> "The maximum time limit on an operation(upload,download,delete,etc.) has been excceded.Try again."
                        ERROR_INVALID_CHECKSUM -> "File on the client does not match the checksum of the file received by the server . Try uploading again ."
                        ERROR_CANCELED -> "User canceled the operation."
                        else -> "An unknown error occurred."
                    }
                    DownloadDocumentService.Response.ErrorResponse(
                        DownloadDocumentService.Error.Http(cause.errorCode, msg)
                    )
                }
                else -> {
                    DownloadDocumentService.Response.ErrorResponse(
                        DownloadDocumentService.Error.Unexpected(
                            cause.message,
                            cause
                        )
                    )
                }
            }
        }

    }

    private fun response(json: List<BookJSON>): List<Read> {
        val reads = mutableListOf<Read>()
        val modifiedAt = ModifiedAt(dateFormatter.getDateString())
        json.forEach { jsonBook ->
            val book =
                Read.Content.Book(
                    idGenerator.incrementAndGet(),
                    jsonBook.name,
                    jsonBook.abbrev,
                    modifiedAt
                )
            reads.add(book)
            jsonBook.chapters.forEachIndexed { index, jsonChapter ->
                val chapter = Read.Content.Chapter(
                    ChapterKey(idGenerator.incrementAndGet(), book.id),
                    index + 1,
                    book.name,
                    modifiedAt
                )
                reads.add(chapter)
                jsonChapter.forEachIndexed { index, jsonVerset ->
                    val verset = Read.Verset(
                        VersetKey(idGenerator.incrementAndGet(), book.id, chapter.id, ""),
                        index + 1,
                        book.abbreviation,
                        chapter.number,
                        jsonVerset,
                        false,
                        modifiedAt
                    )
                    reads.add(verset)
                }
            }
        }
        return reads
    }

    @Suppress("unused")
    private class BookJSON {
        lateinit var abbrev: String
        lateinit var chapters: List<List<String>>
        lateinit var name: String
    }

}