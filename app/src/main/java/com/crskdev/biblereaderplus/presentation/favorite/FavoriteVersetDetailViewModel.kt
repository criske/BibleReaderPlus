/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crskdev.biblereaderplus.domain.entity.SelectedVerset
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.interactors.favorite.FavoriteActionsVersetInteractor
import com.crskdev.biblereaderplus.domain.interactors.favorite.FavoriteVersetInteractor
import com.crskdev.biblereaderplus.domain.interactors.tag.FetchTagsInteractor
import com.crskdev.biblereaderplus.presentation.common.CharSequenceTransformerFactory
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by Cristian Pela on 02.12.2018.
 */
interface FavoriteVersetDetailViewModel {

    val versetKey: VersetKey

    val versetDetailLiveData: LiveData<SelectedVersetUI>

    val versetTagsLiveData: LiveData<List<Tag>>

    val searchTagsLiveData: LiveData<List<Tag>>

    fun addToFavorite(add: Boolean)

    fun searchTagsWith(name: String?)

    fun createTag(tagName: String)

    fun tagVersetAction(tagId: String, add: Boolean)

}

class FavoriteVersetDetailViewModelImpl(override val versetKey: VersetKey,
                                        private val favoriteActionsVersetInteractor: FavoriteActionsVersetInteractor,
                                        private val favoriteVersetInteractor: FavoriteVersetInteractor,
                                        private val charSequenceTransformerFactory: CharSequenceTransformerFactory,
                                        private val tagsInteractor: FetchTagsInteractor)
    : CoroutineScopedViewModel(Dispatchers.Main), FavoriteVersetDetailViewModel {

    private val selectedVersetLiveData: MutableLiveData<SelectedVerset> = MutableLiveData()

    override val versetDetailLiveData: LiveData<SelectedVersetUI> = selectedVersetLiveData.map {
        SelectedVersetUI(
            it.key,
            "${it.bookName} ${it.chapterNumber}:${it.number}",
            charSequenceTransformerFactory
                .startChain(it.contents)
                .transform(CharSequenceTransformerFactory.Type.LEAD_FIRST_LINE)
                .content,
            it.isFavorite
        )
    }

    override val versetTagsLiveData: LiveData<List<Tag>> = selectedVersetLiveData.map {
        it.tags
    }

    override val searchTagsLiveData: LiveData<List<Tag>> = MutableLiveData<List<Tag>>()

    init {
        launch {
            favoriteVersetInteractor.request(versetKey) {
                selectedVersetLiveData.postValue(it)
            }
        }
    }

    override fun addToFavorite(add: Boolean) {
        launch {
            favoriteActionsVersetInteractor.request(
                versetKey,
                FavoriteActionsVersetInteractor.Action.FavoriteAction(add)
            )
        }
    }

    override fun tagVersetAction(tagId: String, add: Boolean) {
        launch {
            favoriteActionsVersetInteractor.request(
                versetKey,
                FavoriteActionsVersetInteractor.Action.TagAction(tagId, add)
            )
        }
    }

    override fun searchTagsWith(name: String?) {
        launch {
            tagsInteractor.request(name).apply {
                (searchTagsLiveData as MutableLiveData<List<Tag>>).postValue(this)
            }
        }
    }

    override fun createTag(tagName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}

data class SelectedVersetUI(val key: VersetKey,
                            val title: String,
                            val formattedContents: CharSequence,
                            val isFavorite: Boolean = false)