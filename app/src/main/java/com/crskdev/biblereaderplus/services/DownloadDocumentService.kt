/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.services

import android.content.Context
import com.crskdev.biblereaderplus.domain.gateway.DownloadDocumentService
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageException.*
import java.io.File

/**
 * Created by Cristian Pela on 07.01.2019.
 */
class DownloadDocumentServiceImpl(context: Context) : DownloadDocumentService {

    private val firebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val cachedFile = File.createTempFile("ro_cornilescu", "json", context.cacheDir)

    override fun download(): DownloadDocumentService.Response {

        val storeRef = firebaseStorage
            .getReferenceFromUrl("gs://biblereaderplus.appspot.com/")
            .child("ro_cornilescu.json")

        return try {
            Tasks.await(storeRef.getFile(cachedFile))
            DownloadDocumentService.Response.OKResponse(emptyList())
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
}