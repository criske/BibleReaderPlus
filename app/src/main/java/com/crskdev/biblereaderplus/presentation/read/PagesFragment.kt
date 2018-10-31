/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.presentation.util.arch.viewModelFromProvider
import kotlinx.android.synthetic.main.fragment_pages.*
import kotlin.random.Random

class PagesFragment : Fragment() {

    private lateinit var sharedViewModel: ReadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = viewModelFromProvider(parentFragment!!) {
            ReadViewModel()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.tag = 100f
        sharedViewModel.scrollPagesLiveData.observe(this, Observer {
            btnReadPage.tag = it
            txtReadPage.text = it.toString()
        })
        btnReadPage.setOnClickListener {
            sharedViewModel.scroll(100, Random.nextInt(0, 100))
        }
    }

}

