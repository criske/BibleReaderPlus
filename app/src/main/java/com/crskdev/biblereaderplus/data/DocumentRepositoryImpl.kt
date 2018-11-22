/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.data

import androidx.paging.DataSource
import androidx.paging.PagedList
import com.crskdev.arch.coroutines.paging.dataSourceFactory
import com.crskdev.biblereaderplus.common.util.pagedlist.InMemoryPagedListDataSource
import com.crskdev.biblereaderplus.domain.entity.*
import com.crskdev.biblereaderplus.domain.gateway.DocumentRepository
import java.util.*

/**
 * Created by Cristian Pela on 21.11.2018.
 */
class DocumentRepositoryImpl : DocumentRepository {

    override fun save(reads: List<Read>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun read(reader: (PagedList<Read>) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun contents(): List<Read.Content> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun filter(query: String): List<Read.Content> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getVerset(versetKey: VersetKey): SelectedVerset? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getVersetProps(versetKey: VersetKey): VersetProps {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun synchronize() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun favoriteAction(versetKey: VersetKey, add: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun favorites(filter: FavoriteFilter): DataSource.Factory<Int, Read.Verset> {
        val r = Random()
        fun versetContent() = sequence {
            while (true) {
                yield((r.nextInt(24) + 97).toChar())
            }
        }.take(r.nextInt(60) + 300)
            .joinToString("")

        val filerFactory: (FavoriteFilter) -> DataSource.Factory<Int, Read.Verset> = {
            (1..100)
                .map { id ->
                    Read.Verset(
                        VersetKey(id, 1, 1),
                        id,
                        "${it::class.java.simpleName}:${versetContent()}",
                        false,
                        ModifiedAt("")
                    )
                }
                .let { l ->
                    dataSourceFactory { InMemoryPagedListDataSource(l) }
                }
        }
        return filerFactory(filter)
    }
}