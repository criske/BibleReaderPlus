/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */
package com.crskdev.biblereaderplus.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.di.Injectable


class MainActivity : AppCompatActivity(), Injectable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
