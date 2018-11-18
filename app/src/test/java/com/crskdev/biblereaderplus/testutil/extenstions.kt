package com.crskdev.biblereaderplus.testutil

/**
 * Created by Cristian Pela on 16.11.2018.
 */
inline fun <T> collectEmitted(block: MutableList<T>.() -> Unit): List<T> =
    mutableListOf<T>().apply(block)

fun List<*>.classesName(): List<String?> = map {
    it!!::class.simpleName
}
