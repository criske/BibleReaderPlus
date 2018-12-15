/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.crskdev.biblereaderplus.common.util.ChannelWith
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.domain.entity.FavoriteFilter
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.domain.interactors.favorite.FavoriteActionsVersetInteractor
import com.crskdev.biblereaderplus.domain.interactors.favorite.FetchFavoriteVersetsInteractor
import com.crskdev.biblereaderplus.domain.interactors.tag.FetchTagsInteractor
import com.crskdev.biblereaderplus.presentation.common.CharSequenceTransformerFactory
import com.crskdev.biblereaderplus.presentation.common.HighLightContentTransformer
import com.crskdev.biblereaderplus.presentation.util.arch.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

interface FavoriteVersetsViewModel : RestorableViewModel<FavoriteFilter?> {

    val versetsLiveData: LiveData<PagedList<Read.Verset>>

    fun currentFilterLiveData(): LiveData<FavoriteFilter>

    fun filter(source: FilterSource)

    fun favoriteAction(versetKey: VersetKey, add: Boolean)

    fun tagToFavoriteAction(versetKey: VersetKey, tagId: String, add: Boolean)

}

sealed class FilterSource {
    class Query(val query: String?) : FilterSource()
    class TagAction(val tag: Tag, val add: Boolean = true) : FilterSource()
    object Order : FilterSource()
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class FavoriteVersetsViewModelImpl(mainDispatcher: CoroutineDispatcher,
                                   private val charSequenceTransformerFactory: CharSequenceTransformerFactory,
                                   private val versetsInteractor: FetchFavoriteVersetsInteractor,
                                   private val fetchTagsInteractor: FetchTagsInteractor,
                                   private val favoriteInteractor: FavoriteActionsVersetInteractor) :
    CoroutineScopedViewModel(mainDispatcher), FavoriteVersetsViewModel {

    private var savingInstanceForKillProcess: FavoriteFilter? = null

    private val filterLiveData: MutableLiveData<FavoriteFilter> = MutableLiveData()

    override val versetsLiveData: LiveData<PagedList<Read.Verset>> = MutableLiveData()

    init {
        launch {
            //throttled inputs 300ms+
            filterLiveData.interval(300, TimeUnit.MILLISECONDS)
                .onNext {
                    savingInstanceForKillProcess = it
                }
                .distinctUntilChanged { prev, curr ->
                    prev.query != curr.query
                            || prev.asc != curr.asc
                            || prev.tags.map { it.id } != curr.tags.map { it.id }
                }
                .toChannel {
                    val mapper: (FavoriteFilter, Read.Verset) -> Read.Verset = { _, v ->
                        val chain = charSequenceTransformerFactory
                            .startChain(v.content)
                            .transform(CharSequenceTransformerFactory.Type.LEAD_FIRST_LINE)
                            .transform(
                                CharSequenceTransformerFactory.Type.HIGHLIGHT,
                                HighLightContentTransformer.HighlightArg(
                                    "None",
                                    true
                                )
                            )
                            .let {
                                if (v.isFavorite) {
                                    it.transform(CharSequenceTransformerFactory.Type.ICON_AT_END)
                                } else {
                                    it
                                }
                            }
                        v.copy(content = chain.content)
                    }
                    versetsInteractor.request(it, mapper) {
                        versetsLiveData.cast<MutableLiveData<PagedList<Read.Verset>>>()
                            .postValue(it)
                    }
                }
        }
        //observe the changes done to tags on the backend(ie. rename, delete), then update the
        //local filter data accordingly
        launch {
            fetchTagsInteractor.request(ChannelWith()) { observedTags ->
                val existentTags = filterLiveData.value?.tags ?: emptySet()
                val updatedTags = mutableSetOf<Tag>().apply {
                    existentTags.forEach { t ->
                        observedTags.find { t.id == it.id }?.let {
                            add(it)
                        }
                    }
                }
                filterLiveData.postValue(filterLiveData.value?.copy(tags = updatedTags))
            }
        }
    }

    override fun getSavingInstanceForKillProcess(): FavoriteFilter? = savingInstanceForKillProcess

    override fun filter(source: FilterSource) {
        //composition
        val value = filterLiveData.value
            ?: throw IllegalStateException("filter live-data must have value")
        filterLiveData.value = when (source) {
            is FilterSource.Query -> value.copy(source.query)
            is FilterSource.TagAction -> if (source.add) {
                value.copy(tags = value.tags + source.tag)
            } else {
                value.copy(tags = value.tags - source.tag)
            }
            is FilterSource.Order -> value.copy(asc = !value.asc)
        }
    }


    override fun favoriteAction(versetKey: VersetKey, add: Boolean) {
        launch {
            favoriteInteractor.request(
                versetKey,
                FavoriteActionsVersetInteractor.Action.Favorite(add)
            )
        }
    }

    override fun tagToFavoriteAction(versetKey: VersetKey, tagId: String, add: Boolean) {
        launch {
            favoriteInteractor.request(
                versetKey, FavoriteActionsVersetInteractor.Action.TagToFavorite(
                    tagId, add
                )
            )
        }
    }

    override fun currentFilterLiveData(): LiveData<FavoriteFilter> = filterLiveData

    override fun restore(data: FavoriteFilter?) {
        if (savingInstanceForKillProcess == null) {
            val filter = data ?: FavoriteFilter.NONE
            filterLiveData.value = filter
        }
    }

}