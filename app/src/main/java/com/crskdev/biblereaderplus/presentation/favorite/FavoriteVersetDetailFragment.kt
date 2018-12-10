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
import androidx.transition.AutoTransition
import androidx.transition.ChangeTransform
import androidx.transition.TransitionSet
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.castIf
import com.crskdev.biblereaderplus.presentation.util.system.getColorCompat
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_favorite_verset_detail.*
import kotlinx.android.synthetic.main.title_layout_default_content.*
import kotlinx.android.synthetic.main.titled_layout.*
import javax.inject.Inject

class FavoriteVersetDetailFragment : DaggerFragment() {

    @Inject
    lateinit var viewModel: FavoriteVersetDetailViewModel

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
        val args = FavoriteVersetDetailFragmentArgs.fromBundle(arguments)
        ViewCompat.setTransitionName(view, args.transitionName)
        (view.parent as? ViewGroup)?.doOnPreDraw {
            // Parent has been drawn. Start transitioning!
            startPostponedEnterTransition()
        }
        viewModel.versetDetailLiveData.observe(this, Observer {
            textTitledLayoutDefault.text = it.formattedContents
            titled_layout_text_title.text = it.title
            with(btnVersetDetailFav) {
                colorFilter = PorterDuff.Mode.SRC_ATOP.toColorFilter(
                    view.context.getColorCompat(
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

        btnVersetDetailFav.setOnClickListener {
            viewModel.addToFavorite(it.tag.castIf<Boolean>()?.not() ?: false)
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
