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
import com.crskdev.biblereaderplus.presentation.common.CharSequenceTransformerFactory
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch

/**
 * Created by Cristian Pela on 02.12.2018.
 */
interface FavoriteVersetDetailViewModel {

    val versetKey: VersetKey

    val versetDetailLiveData: LiveData<SelectedVersetUI>

    val versetTagsLiveData: LiveData<List<Tag>>

    fun addToFavorite(add: Boolean)

    fun tagToFavoriteAction(tagId: String, add: Boolean)

}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class FavoriteVersetDetailViewModelImpl(override val versetKey: VersetKey,
                                        private val favoriteActionsVersetInteractor: FavoriteActionsVersetInteractor,
                                        private val favoriteVersetInteractor: FavoriteVersetInteractor,
                                        private val charSequenceTransformerFactory: CharSequenceTransformerFactory)
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


    private val querySearchTagsLiveData: MutableLiveData<String?> = MutableLiveData()

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
                FavoriteActionsVersetInteractor.Action.Favorite(add)
            )
        }
    }

    override fun tagToFavoriteAction(tagId: String, add: Boolean) {
        launch {
            favoriteActionsVersetInteractor.request(
                versetKey,
                FavoriteActionsVersetInteractor.Action.TagToFavorite(tagId, add)
            )
        }
    }
}


data class SelectedVersetUI(val key: VersetKey,
                            val title: String,
                            val formattedContents: CharSequence,
                            val isFavorite: Boolean = false)