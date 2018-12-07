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
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.presentation.common.TagsSearchView
import com.crskdev.biblereaderplus.presentation.common.parcelize
import com.crskdev.biblereaderplus.presentation.util.system.getParcelableMixin
import com.crskdev.biblereaderplus.presentation.util.view.ADDED_SEARCH_ID
import com.crskdev.biblereaderplus.presentation.util.view.addSearch
import com.crskdev.biblereaderplus.presentation.util.view.findActionView
import com.crskdev.biblereaderplus.presentation.util.view.setup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_search_favorite.*
import javax.inject.Inject

class FavoriteVersetsFragment : DaggerFragment() {

    @Inject
    lateinit var viewModel: FavoriteVersetsViewModel

    private val tagsSearchBottomSheetDialogHelper by lazy {
        TagsSearchBottomSheetDialogHelper(context!!) {
            when (it) {
                is TagsSearchView.Action.Query -> {
                    viewModel.searchTagsWith(it.query)
                }
                is TagsSearchView.Action.Select -> {
                    viewModel.filter(FilterSource.TagAction(it.tag, true))
                }
                is TagsSearchView.Action.Add -> {
                    viewModel.createTag(it.tagName)
                }
            }
        }
    }

    companion object {
        private const val KEY_SI_FILTER = "KEY_SI_FILTER"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(tagsSearchBottomSheetDialogHelper)
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
            when (it) {
                is FavoriteAction.Info -> findNavController().navigate(
                    FavoriteVersetsFragmentDirections
                        .actionFavoriteVersetsFragmentToFavoriteVersetDetailFragment(
                            it.key.parcelize(),
                            it.content.toString(), it.transitionInfo.second
                        ),
                    FragmentNavigatorExtras(it.transitionInfo)
                )
                is FavoriteAction.Add -> viewModel.favoriteAction(it.key, true)
                is FavoriteAction.Remove -> viewModel.favoriteAction(it.key, false)
            }
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
        //selected tags recycler stuff
        val selectedTagsAdapter = TagsAdapter(inflater, TagBehaviour(isClosable = true)) { t, _ ->
            viewModel.filter(FilterSource.TagAction(t, add = false))
        }
        with(recyclerFavoritesSelectedTags) {
            adapter = selectedTagsAdapter
        }

        //toolbar
        with(toolbarFavorites) {
            setup(R.menu.menu_favorites) {
                when (it.itemId) {
                    R.id.menu_action_sort -> {
                        viewModel.filter(FilterSource.Order)
                    }
                    R.id.menu_action_tags -> {
                        tagsSearchBottomSheetDialogHelper.toggleBottomSheet()
                    }
                }
                true
            }
            menu.addSearch(context, R.string.search, onClear = { viewModel.filter(FilterSource.Query(null)) }) {
                viewModel.filter(FilterSource.Query(it))
            }
        }
        //interactions with vm
        viewModel.versetsLiveData.observe(this, Observer {
            favoriteVersetKeyProvider.list = it.snapshot()
            favoritesAdapter.submitList(it)
        })
        viewModel.currentFilterLiveData().observe(this, Observer {
            toolbarFavorites
                .menu
                .findActionView<SearchView>(ADDED_SEARCH_ID)
                .setQuery(it.query, false)
            recyclerFavoritesSelectedTags.isVisible = it.tags.isNotEmpty()
            selectedTagsAdapter.submitList(it.tags.toList())
        })
        viewModel.searchTagsLiveData.observe(this, Observer {
            tagsSearchBottomSheetDialogHelper.submitSuggestions(it)
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


