/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.common.util

import java.util.*

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

inline fun <reified E : Enum<E>, V> enumMap() = EnumMap<E, V>(E::class.java)

inline fun <E : Enum<E>, V> enumMap(block: MutableMap<E, V>.() -> Unit) = EnumMap<E, V>(
    mutableMapOf<E, V>().apply(block)
)

fun Any.println() = println(this)