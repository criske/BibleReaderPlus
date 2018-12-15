/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.entity

/**
 * Created by Cristian Pela on 18.11.2018.
 */
data class Tag(val id: String, val name: String, val color: String = "#000000") {
    companion object {
        fun crateTransientTag(name: String): Tag = Tag("", name)
    }
}


sealed class TagOp(val id: String) {
    class Add(id: String) : TagOp(id)
    class Remove(id: String) : TagOp(id)
    class Delete(id: String) : TagOp(id)
    class Rename(id: String, val newName: String) : TagOp(id)
    class Color(id: String, val color: String) : TagOp(id)
    class Create(val name: String) : TagOp("")
}