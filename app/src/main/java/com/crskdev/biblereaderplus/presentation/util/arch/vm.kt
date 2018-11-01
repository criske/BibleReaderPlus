/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */
package com.crskdev.biblereaderplus.presentation.util.arch

import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext


/**
 * Created by Cristian Pela on 31.10.2018.
 */
inline fun <reified V : ViewModel> viewModelFromProvider(activity: FragmentActivity, crossinline provider: () -> V): V =
    ViewModelProviders.of(activity, object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return provider() as T
        }
    }).get(V::class.java)

inline fun <reified V : ViewModel> viewModelFromProvider(fragment: Fragment, crossinline provider: () -> V): V =
    ViewModelProviders.of(fragment, object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return provider() as T
        }
    }).get(V::class.java)


open class CoroutineScopedViewModel() : ViewModel(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    @CallSuper
    override fun onCleared() {
        job.cancel()
    }
}