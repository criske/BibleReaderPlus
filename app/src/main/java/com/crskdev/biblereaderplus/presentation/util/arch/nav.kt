/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.arch

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment

/**
 * Created by Cristian Pela on 05.11.2018.
 */
fun dynamicallyLoadNavGraph(@IdRes containerId: Int,
                            @IdRes startDestination: Int,
                            @NavigationRes graphId: Int,
                            fragmentManager: FragmentManager,
                            savedInstanceState: Bundle?,
                            arguments: Bundle? = null): NavHostFragment {
    val navHostFragment: NavHostFragment
    if (savedInstanceState == null) {
        navHostFragment = NavHostFragment()
        fragmentManager.beginTransaction()
            .replace(containerId, navHostFragment)
            .setPrimaryNavigationFragment(navHostFragment)
            .commitNow()
    } else {
        navHostFragment = fragmentManager.primaryNavigationFragment!! as NavHostFragment
    }
    navHostFragment.navController.attachNavGraph(graphId) {
        this.startDestination = startDestination
        arguments?.let { addDefaultArguments(it) }
    }
    return navHostFragment
}

inline fun NavController.attachNavGraph(@NavigationRes graphId: Int, customize: NavGraph.() -> Unit) {
    val inflatedGraph = navInflater.inflate(graphId)
    inflatedGraph.customize()
    graph = inflatedGraph
}