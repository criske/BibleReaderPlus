/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.common.util

/**
 * Created by Cristian Pela on 31.10.2018.
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> Any.cast(): T = this as T

inline fun <reified T> Any.castOrNull(): T? = takeIf { it is T }?.cast<T>()

@Suppress("UNCHECKED_CAST")
inline fun <reified T> Any.castIf(): T? =
    try {
        this as T
    } catch (ex: Exception) {
        null
    }

infix fun Any?.ifNull(block: () -> Unit) {
    if (this == null) block()
}

fun Any.println() = println(this)