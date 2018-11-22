/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.common.util

/**
 * Created by Cristian Pela on 31.10.2018.
 */
interface NowTimeProvider {
    companion object {
        val DEFAULT = object : NowTimeProvider {}
    }

    fun now() = System.currentTimeMillis()
}