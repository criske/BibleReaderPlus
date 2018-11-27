/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite


import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.domain.entity.FavoriteFilter
import com.crskdev.biblereaderplus.domain.entity.Read
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.interactors.favorite.FetchFavoriteVersetsInteractor
import com.crskdev.biblereaderplus.presentation.common.CharSequenceTransformerFactory
import com.crskdev.biblereaderplus.presentation.common.HighLightContentTransformer
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.RestorableViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.interval
import com.crskdev.biblereaderplus.presentation.util.arch.onNext
import com.crskdev.biblereaderplus.presentation.util.system.getParcelableMixin
import com.crskdev.biblereaderplus.presentation.util.view.addSearch
import com.crskdev.biblereaderplus.presentation.util.view.setup
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

    companion object {
        private const val KEY_SI_FILTER = "KEY_SI_FILTER"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //recycler stuff
        val favoritesAdapter = FavoriteVersetsAdapter(LayoutInflater.from(context)) {
            val todo = when (it) {
                is FavoriteAction.Info -> "TODO: Show info: ${it.key}"
                is FavoriteAction.Add -> "TODO: Add fav: ${it.key}"
                is FavoriteAction.Remove -> "TODO: Remove fav: ${it.key}"
            }
            Toast.makeText(context, todo, Toast.LENGTH_SHORT).show()
        }
        val favoriteVersetKeyProvider = FavoriteVersetKeyProvider()
        recyclerFavorites.apply {
            adapter = favoritesAdapter
            val selectionTracker = SelectionTracker.Builder<String>(
                "fav-verset-select-tracker",
                this,
                favoriteVersetKeyProvider,
                FavoriteVersetLookup(this),
                StorageStrategy.createStringStorage()
            ).withGestureTooltypes(MotionEvent.TOOL_TYPE_FINGER)
                .withSelectionPredicate(SelectionPredicates.createSelectSingleAnything())
                .build().apply {
                    onRestoreInstanceState(savedInstanceState)
                }
            favoritesAdapter.selectionTracker = selectionTracker
        }

        //toolbar
        with(toolbarFavorites) {
            setup {
                true
            }
            menu.addSearch(context, R.string.search) {

            }
        }

        //buttons
        button.setOnClickListener {
            viewModel.filter(
                listOf(
                    FavoriteFilter.None(),
                    FavoriteFilter.ByTag(Tag("foo")),
                    FavoriteFilter.Query("foo")
                ).random()
            )
        }

        //interactions with vm
        viewModel.versetsLiveData.observe(this, Observer {
            favoriteVersetKeyProvider.list = it.snapshot()
            favoritesAdapter.submitList(it)
        })
        viewModel.restore(
            savedInstanceState
                ?.getParcelableMixin<ParcelableFavoriteFilter>(KEY_SI_FILTER)
                ?.deparcelize()
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(
            KEY_SI_FILTER,
            viewModel.getSavingInstanceForKillProcess()?.parcelize()
        )
        super.onSaveInstanceState(outState)
    }
}


interface FavoriteVersetsViewModel : RestorableViewModel<FavoriteFilter?> {
    val versetsLiveData: LiveData<PagedList<Read.Verset>>
    fun filter(filter: FavoriteFilter = FavoriteFilter.None())
}

@ObsoleteCoroutinesApi
class FavoriteVersetsViewModelImpl(mainDispatcher: CoroutineDispatcher,
                                   private val charSequenceTransformerFactory: CharSequenceTransformerFactory,
                                   private val interactor: FetchFavoriteVersetsInteractor) :
    CoroutineScopedViewModel(mainDispatcher), FavoriteVersetsViewModel {

    private var savingInstanceForKillProcess: FavoriteFilter? = null

    override val versetsLiveData: LiveData<PagedList<Read.Verset>> =
        MutableLiveData<PagedList<Read.Verset>>()

    private val filterLiveData: MutableLiveData<FavoriteFilter> = MutableLiveData()

    init {
        launch {
            val filterChannel = actor<FavoriteFilter> {
                val mapper: (FavoriteFilter, Read.Verset) -> Read.Verset = { _, v ->
                    val chain = charSequenceTransformerFactory
                        .startChain(v.content)
                        .transform(CharSequenceTransformerFactory.Type.LEAD_FIRST_LINE)
                        .transform(
                            CharSequenceTransformerFactory.Type.HIGHLIGHT,
                            HighLightContentTransformer.HighlightArg("None", true)
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
                interactor.request(channel, mapper) {
                    versetsLiveData.cast<MutableLiveData<PagedList<Read.Verset>>>().value = it
                }
            }
            //throttled filter
            filterLiveData.interval(300, TimeUnit.MILLISECONDS)
                .onNext {
                    savingInstanceForKillProcess = it
                }
                .observeForever {
                    launch {
                        filterChannel.send(it)
                    }
                }
        }
    }

    override fun getSavingInstanceForKillProcess(): FavoriteFilter? = savingInstanceForKillProcess

    override fun filter(filter: FavoriteFilter) {
        filterLiveData.value = filter
    }

    override fun restore(filter: FavoriteFilter?) {
        if (savingInstanceForKillProcess == null) {
            val safeFilter = filter ?: FavoriteFilter.None()
            filter(safeFilter)
        }
    }
}
