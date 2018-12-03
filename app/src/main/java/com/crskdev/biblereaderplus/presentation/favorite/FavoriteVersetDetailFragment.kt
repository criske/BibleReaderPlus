/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.transition.AutoTransition
import androidx.transition.ChangeTransform
import androidx.transition.TransitionSet
import com.crskdev.biblereaderplus.R
import kotlinx.android.synthetic.main.fragment_favorite_verset_detail.*

class FavoriteVersetDetailFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = DetailsTransition().apply {
            duration = 300
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite_verset_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()
        val args = FavoriteVersetDetailFragmentArgs.fromBundle(arguments)
        ViewCompat.setTransitionName(view, args.transitionName)
        textVersetDetail.text = args.content
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

