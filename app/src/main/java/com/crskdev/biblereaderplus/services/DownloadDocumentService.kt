/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.services

import com.crskdev.biblereaderplus.domain.gateway.DownloadDocumentService
import java.lang.Thread.sleep

/**
 * Created by Cristian Pela on 07.01.2019.
 */
class DownloadDocumentServiceImpl : DownloadDocumentService {
    override fun download(): DownloadDocumentService.Response {
        sleep(2000)
        return DownloadDocumentService.Response.OKResponse(emptyList())
    }
}