/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.interactors.read.ContentInteractor
import com.crskdev.biblereaderplus.presentation.util.arch.*
import kotlinx.coroutines.launch

class ContentsViewModel(private val contentInteractor: ContentInteractor) :
    CoroutineScopedViewModel() {

    private val searchBookLiveData: MutableLiveData<String> = MutableLiveData<String>()
        .apply { value = "" }

    private val contentLiveData: LiveData<List<ReadUI.ContentUI>> =
        MutableLiveData<List<ReadUI.ContentUI>>()

    private val scrollReadLiveData: MutableLiveData<ReadKey> = MutableLiveData<ReadKey>()
        .apply {
            value = ReadKey.INITIAL
        }

    private val expansionLiveData: MutableLiveData<Expansion> = MutableLiveData<Expansion>()
        .apply {
            value = ReadKey.INITIAL to IsExpanded(true)
        }

    init {
        launch {
            searchBookLiveData.toChannel {
                contentInteractor.request(it) { response ->
                    when (response) {
                        is ContentInteractor.Response.OK -> {
                            val contentsUI = response.result!!.map { read ->
                                when (read) {
                                    is Read.Content.Book -> ReadUI.BookUI(
                                        read.id,
                                        read.name,
                                        HasScrollPosition(
                                            false
                                        ),
                                        IsBookmarked(
                                            false
                                        )
                                    )
                                    is Read.Content.Chapter -> ReadUI.ChapterUI(
                                        read.id,
                                        read.key.bookId,
                                        "Chapter ${read.number}", // TODO i18n pls!
                                        HasScrollPosition(
                                            false
                                        ),
                                        IsBookmarked(
                                            false
                                        )
                                    )
                                }
                            }
                            contentLiveData
                                .cast<MutableLiveData<List<ReadUI.ContentUI>>>()
                                .postValue(contentsUI)
                        }
                        is ContentInteractor.Response.Error -> {
                            //no-op}
                        }
                    }
                }
            }
        }

    }

    private val positionAndSelectionMapping: (Pair<ReadKey, List<ReadUI.ContentUI>>) -> (ReadPosToListReadUI) =
        { pair ->
            val positionKey = pair.first
            var closestContentKey = ReadKey.INITIAL
            var position = closestContentKey()
            val source = pair.second.toMutableList()
            source.forEachIndexed { index, item ->
                closestContentKey = item.getKey()
                if (closestContentKey() <= positionKey()) {
                    position = index
                } else {
                    return@forEachIndexed
                }
            }
            if (source.isNotEmpty()) {
                source[position] = source[position].setHasScrollPosition(true) as ReadUI.ContentUI
            }
            position to source
        }

    private val scrollPositionAndContentsLiveData: LiveData<ReadPosToListReadUI> =
        scrollReadLiveData
            .combineLatest(contentLiveData
                .combineLatest(expansionLiveData)
                .scan(ExpansionAccumulator()) { acc, contentsToExpansion ->
                    val (list, expansion) = contentsToExpansion
                    acc.apply {
                        this.contents = list
                        this.collapsedKeys.apply {
                            if (expansion.second()) {
                                remove(expansion.first())
                            } else {
                                add(expansion.first())
                            }

                        }
                        acc
                    }
                }
                .map { ea ->
                    ea.contents
                        .map {
                            val bookId = if (it is ReadUI.ChapterUI)
                                it.bookId
                            else
                                it.getKey()()
                            it.setExpanded(IsExpanded(!ea.collapsedKeys.contains(bookId)))
                        }
                        .filter { c -> c is ReadUI.BookUI || c is ReadUI.ChapterUI && c.isExpanded() }
                }
            )
            .map(positionAndSelectionMapping)
            .distinctUntilChanged { prev, curr ->
                prev.first != curr.first || prev.second != curr.second
            }


    val scrollPositionLiveData: LiveData<Int> =
        scrollPositionAndContentsLiveData.map { it.first }.filter { it != -1 }
    val contentsLiveData: LiveData<List<ReadUI.ContentUI>> =
        scrollPositionAndContentsLiveData.map { it.second }


    fun scrollTo(readKey: ReadKey) {
        scrollReadLiveData.value = readKey
    }

    fun search(book: String) {
        //TODO condition here
        searchBookLiveData.value = book
    }

    fun expand(readKey: ReadKey, expanded: IsExpanded = IsExpanded(true)) {
        expansionLiveData.value = readKey to expanded
    }

    //Note use int instead of inline ReadKey the comparation is not working in sets
    private class ExpansionAccumulator(var contents: List<ReadUI.ContentUI> = emptyList(),
                                       var collapsedKeys: MutableSet<Int> = mutableSetOf())

}

typealias ReadPosToListReadUI = Pair<Int, List<ReadUI.ContentUI>>
typealias Expansion = Pair<ReadKey, IsExpanded>