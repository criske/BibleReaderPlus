/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.setup

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
import kotlinx.android.synthetic.main.fragment_scaffold_mock.*
import javax.inject.Inject

/**
 * Created by Cristian Pela on 05.11.2018.
 */
class AuthStepFragment : DaggerFragment() {

    @Inject
    lateinit var authStepViewModel: AuthStepViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scaffold_mock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textScaffoldMock.text = authStepViewModel.hello
    }
}


class AuthStepViewModel : ViewModel() {

    val hello = "Hello from AuthStepViewModel"
}