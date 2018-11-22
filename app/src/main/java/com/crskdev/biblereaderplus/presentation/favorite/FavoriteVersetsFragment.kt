/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.common.util.ifNull
import com.crskdev.biblereaderplus.domain.entity.FavoriteFilter
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.interactors.favorite.FetchFavoriteVersetsInteractor
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.interval
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_search_favorite.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FavoriteVersetsFragment : DaggerFragment() {

    @Inject
    lateinit var viewModel: FavoriteVersetsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.versetsLiveData.observe(this, Observer {
            favHello.text = it.map { it?.content.toString() }.toString()
        })
        button.setOnClickListener {
            viewModel.filter(
                listOf(
                    FavoriteFilter.ByLastModified.ASC,
                    FavoriteFilter.ByLastModified.DESC,
                    FavoriteFilter.None,
                    FavoriteFilter.ByTag(Tag("foo")),
                    FavoriteFilter.Query("foo")
                ).random()
            )
        }
        savedInstanceState ifNull {
            viewModel.filter()
        }
    }

}


interface FavoriteVersetsViewModel {
    val versetsLiveData: LiveData<PagedList<Read.Verset>>
    fun filter(filter: FavoriteFilter = FavoriteFilter.None)
}

@ObsoleteCoroutinesApi
class FavoriteVersetsViewModelImpl(mainDispatcher: CoroutineDispatcher,
                                   private val interactor: FetchFavoriteVersetsInteractor) :
    CoroutineScopedViewModel(mainDispatcher), FavoriteVersetsViewModel {

    override val versetsLiveData: LiveData<PagedList<Read.Verset>> =
        MutableLiveData<PagedList<Read.Verset>>()

    private val filterLiveData: MutableLiveData<FavoriteFilter> = MutableLiveData()

    init {
        launch {
            val filterChannel = actor<FavoriteFilter> {
                interactor.request(channel) {
                    versetsLiveData.cast<MutableLiveData<PagedList<Read.Verset>>>().value = it
                }
            }
            //throttled filter
            filterLiveData.interval(300, TimeUnit.MILLISECONDS).observeForever {
                launch {
                    filterChannel.send(it)
                }
            }
        }
    }

    override fun filter(filter: FavoriteFilter) {
        filterLiveData.value = filter
    }
}
