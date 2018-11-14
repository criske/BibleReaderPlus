/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.gateway

import com.crskdev.biblereaderplus.domain.entity.Read

/**
 * Created by Cristian Pela on 06.11.2018.
 */
interface DownloadDocumentService {

    companion object {
        private val EMPTY_DOCUMENT = emptyList<Read>()
    }

    suspend fun download(): DownloadDocumentService.Response

    sealed class Error(val message: String?) {
        object None : Error(null)
        class Conversion(message: String?) : Error(message)
        class Http(val code: Int, message: String?) : Error(message)
        class Network(message: String?) : Error(message)
        class Unexpected(message: String?) : Error(message)
    }

    sealed class Response(val error: DownloadDocumentService.Error, val document: List<Read>) {
        class ErrorResponse(error: DownloadDocumentService.Error) : Response(error, EMPTY_DOCUMENT)
        class OKResponse(document: List<Read>) :
            Response(DownloadDocumentService.Error.None, document)
    }

}