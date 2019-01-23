/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.gateway

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Cristian Pela on 23.01.2019.
 */
@Singleton
class DateFormatter @Inject constructor() {

    private val simpleDateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    }

    fun getDateString(): String = simpleDateFormat.format(Date())

}