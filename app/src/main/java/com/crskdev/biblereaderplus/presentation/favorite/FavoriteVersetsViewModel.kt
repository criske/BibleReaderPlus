/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.domain.entity.*
import com.crskdev.biblereaderplus.domain.interactors.favorite.FavoriteActionsVersetInteractor
import com.crskdev.biblereaderplus.domain.interactors.favorite.FetchFavoriteVersetsInteractor
import com.crskdev.biblereaderplus.domain.interactors.tag.FetchTagsInteractor
import com.crskdev.biblereaderplus.presentation.common.CharSequenceTransformerFactory
import com.crskdev.biblereaderplus.presentation.common.HighLightContentTransformer
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetsViewModel.ErrorVM
import com.crskdev.biblereaderplus.presentation.util.arch.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

interface FavoriteVersetsViewModel : RestorableViewModel<FavoriteFilter?> {

    val versetsLiveData: LiveData<PagedList<Read.Verset>>

    val searchTagsLiveData: SingleLiveEvent<List<Tag>>

    val errorsLiveData: SingleLiveEvent<ErrorVM>

    fun currentFilterLiveData(): LiveData<FavoriteFilter>

    fun filter(source: FilterSource)

    fun favoriteAction(versetKey: VersetKey, add: Boolean)

    fun searchTagsWith(name: String)

    fun createTag(tagName: String)

    fun renameTag(id: String, newName: String)

    sealed class ErrorVM(val err: Throwable?) {
        object EmptyTagName : ErrorVM(null)
        object ShortTagName : ErrorVM(null)
        class Unknown(err: Throwable?) : ErrorVM(err)
    }
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
                                   private val tagsInteractor: FetchTagsInteractor,
                                   private val favoriteInteractor: FavoriteActionsVersetInteractor) :
    CoroutineScopedViewModel(mainDispatcher), FavoriteVersetsViewModel {

    private var savingInstanceForKillProcess: FavoriteFilter? = null

    private val filterLiveData: MutableLiveData<FavoriteFilter> = MutableLiveData()

    private val querySearchTagsLiveData: MutableLiveData<String?> = MutableLiveData()

    override val searchTagsLiveData: SingleLiveEvent<List<Tag>> = SingleLiveEvent()

    override val versetsLiveData: LiveData<PagedList<Read.Verset>> = MutableLiveData()

    override val errorsLiveData: SingleLiveEvent<ErrorVM> = SingleLiveEvent()

    private val errorHandler: (FavoriteActionsVersetInteractor.ResponseError) -> Unit = {
        val err = when (it) {
            FavoriteActionsVersetInteractor.ResponseError.EmptyTagName -> ErrorVM.EmptyTagName
            FavoriteActionsVersetInteractor.ResponseError.ShortTagName -> ErrorVM.ShortTagName
            is FavoriteActionsVersetInteractor.ResponseError.Unknown -> ErrorVM.Unknown(
                it.err
            )
        }
        errorsLiveData.postValue(err)
    }

    init {
        launch {
            //throttled inputs 300ms+
            filterLiveData.interval(300, TimeUnit.MILLISECONDS)
                .onNext {
                    savingInstanceForKillProcess = it
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
        launch {
            querySearchTagsLiveData.interval(300, TimeUnit.MILLISECONDS).toChannel {
                tagsInteractor.request(it) {
                    searchTagsLiveData.cast<MutableLiveData<List<Tag>>>()
                        .postValue(it.toList())
                }
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

    override fun searchTagsWith(name: String) {
        querySearchTagsLiveData.value = name
    }

    override fun favoriteAction(versetKey: VersetKey, add: Boolean) {
        launch {
            favoriteInteractor.request(
                versetKey,
                FavoriteActionsVersetInteractor.Action.FavoriteAction(add),
                errorHandler
            )
        }
    }

    override fun createTag(tagName: String) {
        launch {
            favoriteInteractor.request(
                VersetKey.NONE,
                FavoriteActionsVersetInteractor.Action.TagAction(TagOp.Create(tagName)),
                errorHandler
            )
        }
    }

    override fun renameTag(id: String, newName: String) {
        launch {
            favoriteInteractor.request(
                VersetKey.NONE,
                FavoriteActionsVersetInteractor.Action.TagAction(TagOp.Rename(id, newName)),
                errorHandler
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