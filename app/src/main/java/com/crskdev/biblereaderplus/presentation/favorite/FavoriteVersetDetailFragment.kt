/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite


import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorFilter
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.ChangeTransform
import androidx.transition.TransitionSet
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.castIf
import com.crskdev.biblereaderplus.presentation.tags.*
import com.crskdev.biblereaderplus.presentation.util.arch.navigateUp
import com.crskdev.biblereaderplus.presentation.util.system.getColorCompat
import com.crskdev.biblereaderplus.presentation.util.view.setup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_favorite_verset_detail.*
import kotlinx.android.synthetic.main.title_layout_default_content.*
import kotlinx.android.synthetic.main.titled_layout.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class FavoriteVersetDetailFragment : DaggerFragment() {

    @Inject
    lateinit var viewModel: FavoriteVersetDetailViewModel

    @Inject
    lateinit var tagSelectViewModel: TagSelectViewModel

    @Inject
    lateinit var tagOpsViewModel: TagsOpsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition();
        sharedElementEnterTransition = DetailsTransition().apply {
            duration = 300
        }
        sharedElementReturnTransition = DetailsTransition().apply {
            duration = 300
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite_verset_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        startSharedTransition(view)
        setupUIComponents()
        observeVMEvents()
    }

    private fun observeVMEvents() {
        viewModel.versetDetailLiveData.observe(this, Observer {
            textTitledLayoutDefault.text = it.formattedContents
            titled_layout_text_title.text = it.title
            with(btnVersetDetailFav) {
                colorFilter = PorterDuff.Mode.SRC_ATOP.toColorFilter(
                    context.getColorCompat(
                        if (it.isFavorite) {
                            R.color.likeColor
                        } else {
                            android.R.color.white
                        }
                    )
                )
                tag = it.isFavorite
            }
        })
        viewModel.versetTagsLiveData.observe(this, Observer {
            recyclerVersetDetailTags.adapter?.castIf<TagsAdapter>()?.submitList(it)
        })
        tagSelectViewModel.selectedTagLiveData.observe(this, Observer {
            viewModel.tagToFavoriteAction(it.id, true)
        })
    }

    private fun setupUIComponents() {
        with(toolbarVersetDetail) {
            setup(R.menu.menu_tags) {
                when (it.itemId) {
                    R.id.menu_action_tags -> {
                        TagsSearchBottomSheetDialogFragment.show(childFragmentManager)
                    }
                }
                true
            }
            setNavigationOnClickListener {
                navigateUp()
            }
        }
        with(recyclerVersetDetailTags) {
            adapter = TagsAdapter(
                LayoutInflater.from(context),
                TagBehaviour(isClosable = true)
            ) { t, a ->
                when (a) {
                    TagSelectAction.CLOSE -> {
                        viewModel.tagToFavoriteAction(t.id, false)
                    }
                    TagSelectAction.CONTEXT_MENU_RENAME -> {
                        tagOpsViewModel.renameTag(t.id, t.name)
                    }
                    TagSelectAction.CONTEXT_MENU_REMOVE -> {
                        TagOpsUI.showConfirmationDialogOnDelete(context, t) {
                            tagOpsViewModel.deleteTag(it.id)
                        }
                    }
                    TagSelectAction.CONTEXT_MENU_CHANGE_COLOR -> {

                    }
                    TagSelectAction.SELECT -> {

                    }
                }
            }
            layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        }
        btnVersetDetailFav.setOnClickListener {
            viewModel.addToFavorite(it.tag.castIf<Boolean>()?.not() ?: false)
        }
    }

    private fun startSharedTransition(view: View) {
        val args = FavoriteVersetDetailFragmentArgs.fromBundle(arguments)
        ViewCompat.setTransitionName(view, args.transitionName)
        (view.parent as? ViewGroup)?.doOnPreDraw {
            // Parent has been drawn. Start transitioning!
            startPostponedEnterTransition()
        }
    }
}


class DetailsTransition : TransitionSet() {
    init {
        ordering = ORDERING_TOGETHER;
        addTransition(ChangeTransform())
            .addTransition(AutoTransition())
    }
}
