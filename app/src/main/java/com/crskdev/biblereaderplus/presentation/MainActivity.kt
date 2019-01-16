/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */
package com.crskdev.biblereaderplus.presentation

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.domain.gateway.AuthService
import com.crskdev.biblereaderplus.domain.interactors.setup.CheckInitInteractor
import com.crskdev.biblereaderplus.presentation.awareness.IsPlatformAuthAware
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.dynamicallyLoadNavGraph
import com.crskdev.biblereaderplus.presentation.util.system.onBackPressedToExit
import com.crskdev.biblereaderplus.presentation.util.system.traverseFragments
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.isInitLiveData.observe(this, Observer {
            val loadGraph = if (it) R.id.readFragment else R.id.setupFragment
            dynamicallyLoadNavGraph(
                R.id.container,
                R.navigation.main_nav_graph,
                loadGraph,
                supportFragmentManager,
                savedInstanceState
            )
        })
    }

    override fun onSupportNavigateUp() = findNavController(R.id.container).navigateUp()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthService.PLATFORM_SIGNIN_REQUEST_CODE) {
            traverseFragments {
                if (it is IsPlatformAuthAware) {
                    it.onPlatformAuth(resultCode, data)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!onBackPressedToExit(isUsingNavHostFragment = true)) {
            super.onBackPressed()
        }
    }

}


class MainViewModel(private val checkInitInteractor: CheckInitInteractor) :
    CoroutineScopedViewModel() {

    val isInitLiveData: LiveData<Boolean> = MutableLiveData<Boolean>().apply {
        value = checkInitInteractor.requestIsInitialized()
    }

}