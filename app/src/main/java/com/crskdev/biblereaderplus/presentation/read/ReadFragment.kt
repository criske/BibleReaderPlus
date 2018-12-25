/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.presentation.util.arch.distinctUntilChanged
import dagger.android.support.DaggerFragment

class ReadFragment : DaggerFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_read, container, false)
    }

}

class ReadViewModel : ViewModel() {

    val scrollReadLiveData: LiveData<ScrollData> = MutableLiveData<ScrollData>()
        .distinctUntilChanged { p, c ->
            p.readKey() != c.readKey()
        }

    fun scrollTo(source: Int, readKey: ReadKey) {
        scrollReadLiveData.cast<MutableLiveData<ScrollData>>().value = ScrollData(source, readKey)
    }

    class ScrollData(val source: Int, val readKey: ReadKey)

}

