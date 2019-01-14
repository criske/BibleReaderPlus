/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.services

import com.crskdev.biblereaderplus.domain.gateway.DownloadDocumentService

/**
 * Created by Cristian Pela on 07.01.2019.
 */
class DownloadDocumentServiceImpl : DownloadDocumentService {
    override suspend fun download(): DownloadDocumentService.Response =
        DownloadDocumentService.Response.OKResponse(emptyList())
}