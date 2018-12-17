/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.presentation.favorite


import android.os.Bundle
import android.view.LayoutInflater
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
import com.crskdev.biblereaderplus.presentation.tags.*
import com.crskdev.biblereaderplus.presentation.util.arch.navigateUp
import com.crskdev.biblereaderplus.presentation.util.system.getParcelableMixin
import com.crskdev.biblereaderplus.presentation.util.view.ADDED_SEARCH_ID
import com.crskdev.biblereaderplus.presentation.util.view.addSearch
import com.crskdev.biblereaderplus.presentation.util.view.findActionView
import com.crskdev.biblereaderplus.presentation.util.view.setup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_search_favorite.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class FavoriteVersetsFragment : DaggerFragment() {

    @Inject
    lateinit var viewModel: FavoriteVersetsViewModel

    @Inject
    lateinit var tagSelectViewModel: TagSelectViewModel

    @Inject
    lateinit var tagOpsViewModel: TagsOpsViewModel

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
            when (it) {
                is FavoriteAction.Info -> findNavController().navigate(
                    FavoriteVersetsFragmentDirections
                        .actionFavoriteVersetsFragmentToFavoriteVersetDetailFragment(
                            it.transitionInfo.versetId,
                            it.transitionInfo.content,
                            it.transitionInfo.name
                        ),
                    FragmentNavigatorExtras(it.transitionInfo())
                )
                is FavoriteAction.Add -> viewModel.favoriteAction(it.id, true)
                is FavoriteAction.Remove -> viewModel.favoriteAction(it.id, false)
            }
        }
        val favoriteVersetKeyProvider = FavoriteVersetKeyProvider()
        recyclerFavorites.apply {
            adapter = favoritesAdapter
            val selectionTracker = SelectionTracker
                .Builder<String>(
                    "fav-verset-select-tracker",
                    this,
                    favoriteVersetKeyProvider,
                    FavoriteVersetLookup(this),
                    StorageStrategy.createStringStorage()
                )
                .withSelectionPredicate(SelectionPredicates.createSelectSingleAnything())
                .build().apply {
                    onRestoreInstanceState(savedInstanceState)
                }
            favoritesAdapter.selectionTracker = selectionTracker
        }
        //selected tags recycler stuff
        val selectedTagsAdapter = TagsAdapter(
            inflater,
            TagBehaviour(isClosable = true)
        ) { t, a ->
            when (a) {
                TagSelectAction.CLOSE -> {
                    viewModel.filter(FilterSource.TagAction(t, false))
                }
                TagSelectAction.CONTEXT_MENU_RENAME -> {
                    tagOpsViewModel.renameTag(t.id, t.name)
                }
                TagSelectAction.CONTEXT_MENU_REMOVE -> {
                    tagOpsViewModel.deleteTag(t.id)
                }
                TagSelectAction.CONTEXT_MENU_CHANGE_COLOR -> {
                    tagOpsViewModel.changeColor(t.id, t.color)
                }
                TagSelectAction.SELECT -> {
                }
            }
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
                        TagsSearchBottomSheetDialogFragment.show(childFragmentManager)
                    }
                }
                true
            }
            menu.addSearch(
                context,
                R.string.search,
                onClear = { viewModel.filter(FilterSource.Query(null)) }) {
                viewModel.filter(FilterSource.Query(it))
            }
            setNavigationOnClickListener {
                navigateUp()
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
        viewModel.restore(
            savedInstanceState
                ?.getParcelableMixin<ParcelableFavoriteFilter>(KEY_SI_FILTER)
                ?.deparcelize()
        )
        tagSelectViewModel.selectedTagLiveData.observe(this, Observer {
            viewModel.filter(FilterSource.TagAction(it, true))
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(
            KEY_SI_FILTER,
            viewModel.getSavingInstanceForKillProcess()?.parcelize()
        )
        super.onSaveInstanceState(outState)
    }
}


