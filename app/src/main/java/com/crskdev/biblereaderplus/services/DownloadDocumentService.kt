/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.services

import com.crskdev.biblereaderplus.domain.gateway.DownloadDocumentService
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Cristian Pela on 07.01.2019.
 */
class DownloadDocumentServiceImpl : DownloadDocumentService {

    private val errorCounter = AtomicInteger(0)


    override fun download(): DownloadDocumentService.Response {
        sleep(2000)

        return if (errorCounter.getAndIncrement() < 2) {
            DownloadDocumentService.Response.ErrorResponse(DownloadDocumentService.Error.Network("Network is down"))
        } else {
            DownloadDocumentService.Response.OKResponse(emptyList())
        }
    }
}