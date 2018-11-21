/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */
package com.crskdev.biblereaderplus.presentation

import android.os.Bundle
import androidx.navigation.findNavController
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.presentation.util.arch.dynamicallyLoadNavGraph
import dagger.android.support.DaggerAppCompatActivity


class MainActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dynamicallyLoadNavGraph(
            R.id.container,
            R.navigation.main_nav_graph,
            R.id.favoriteVersetsFragment,
            supportFragmentManager,
            savedInstanceState
        )
    }

    override fun onSupportNavigateUp() = findNavController(R.id.container).navigateUp()

}
