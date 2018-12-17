/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */
package com.crskdev.biblereaderplus.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.navigation.findNavController
import com.crskdev.biblereaderplus.R
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        colorPicker.setOnColorPickListener {
            Toast.makeText(applicationContext, "Selected $it", Toast.LENGTH_SHORT).apply {
                view.setBackgroundColor(it)
            }.show()
        }
//        dynamicallyLoadNavGraph(
//            R.id.container,
//            R.navigation.main_nav_graph,
//            R.id.favoriteVersetsFragment,
//            supportFragmentManager,
//            savedInstanceState
//        )
    }

    override fun onSupportNavigateUp() = findNavController(R.id.container).navigateUp()

}
