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
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetsViewModel.FilterSource
import com.crskdev.biblereaderplus.presentation.util.system.getParcelableMixin
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
            setup(R.menu.menu_favorites) {
                when (it.itemId) {
                    R.id.menu_action_sort -> {
                        viewModel.filter(FilterSource.Order)
                    }
                }
                true
            }
            menu.addSearch(context, R.string.search) {
                viewModel.filter(FilterSource.Query(it))
            }
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


