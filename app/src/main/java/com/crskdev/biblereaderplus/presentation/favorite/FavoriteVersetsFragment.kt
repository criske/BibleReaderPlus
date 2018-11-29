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
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetsViewModel.FilterSource
import com.crskdev.biblereaderplus.presentation.util.system.getParcelableMixin
import com.crskdev.biblereaderplus.presentation.util.view.ADDED_SEARCH_ID
import com.crskdev.biblereaderplus.presentation.util.view.addSearch
import com.crskdev.biblereaderplus.presentation.util.view.setup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_search_favorite.*
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
        //versets recycler stuff
        val inflater = LayoutInflater.from(context)
        val favoritesAdapter = FavoriteVersetsAdapter(inflater) {
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

        //available tags recycler stuff
        val availableTagsAdapter =
            TagsAdapter(inflater, TagBehaviour(isSelectable = true)) { t, _ ->
                viewModel.filter(FilterSource.TagAction(t))
            }
        with(recyclerFavoritesAvailableTags) {
            adapter = availableTagsAdapter
            layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL)
        }


        //selected tags recycler stuff
        val selectedTagsAdapter = TagsAdapter(inflater, TagBehaviour(isClosable = true)) { t, _ ->
            viewModel.filter(FilterSource.TagAction(t, add = false))
        }
        with(scrollChipGroupFavoritesSelectedTags) {
            adapter = selectedTagsAdapter
        }

        //toolbar
        with(toolbarFavorites) {
            setup(R.menu.menu_favorites) {
                when (it.itemId) {
                    R.id.menu_action_sort -> {
                        viewModel.filter(FilterSource.Order)
                    }
                }
                true
            }
            menu.addSearch(context, R.string.search, onClear = {
                viewModel.filter(FilterSource.Query(null))
            }) {
                viewModel.filter(FilterSource.Query(it))
            }
        }
        //interactions with vm
        viewModel.versetsLiveData.observe(this, Observer {
            favoriteVersetKeyProvider.list = it.snapshot()
            favoritesAdapter.submitList(it)
        })
        viewModel.availableTagsLiveData.observe(this, Observer {
            availableTagsAdapter.submitList(it)
        })
        viewModel.currentFilterLiveData().observe(this, Observer {
            toolbarFavorites.menu
                .findItem(ADDED_SEARCH_ID).actionView
                .cast<SearchView>()
                .setQuery(it.query, false)
            selectedTagsAdapter.submitList(it.tags.toList())
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


