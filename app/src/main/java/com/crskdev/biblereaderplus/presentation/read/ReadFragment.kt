/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import com.crskdev.biblereaderplus.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_scaffold_mock.*
import javax.inject.Inject

class ReadFragment : DaggerFragment() {

    @Inject
    lateinit var readViewModel: ReadViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scaffold_mock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textScaffoldMock.text = readViewModel.hello
    }
}

class ReadViewModel @Inject constructor() : ViewModel() {

    val hello = "Hello from ReadViewModel"

}
