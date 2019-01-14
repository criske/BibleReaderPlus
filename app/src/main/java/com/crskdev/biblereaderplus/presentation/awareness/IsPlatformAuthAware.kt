/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.presentation.awareness

import android.content.Intent

/**
 * Created by Cristian Pela on 09.01.2019.
 */
interface IsPlatformAuthAware {
    fun onPlatformAuth(resultCode: Int, data: Intent?)
}