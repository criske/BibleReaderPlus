/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.system

import android.content.Context
import android.util.TypedValue

/**
 * Created by Cristian Pela on 02.11.2018.
 */
inline fun <reified T> Context.getThemeAttribute(id: Int, default: T): T = TypedValue().let {
    theme.resolveAttribute(id, it, true)
    it.data as T
}