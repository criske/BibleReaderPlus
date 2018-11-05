/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read

/**
 * Created by Cristian Pela on 05.11.2018.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import com.crskdev.biblereaderplus.R
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ContentsFragment : DaggerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scaffold_mock, container, false)
    }

}

class ContentsViewModel @Inject constructor() : ViewModel() {

}

