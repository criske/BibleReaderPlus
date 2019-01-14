/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.presentation.util.arch

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController

/**
 * Created by Cristian Pela on 05.11.2018.
 */
fun dynamicallyLoadNavGraph(@IdRes containerId: Int,
                            @NavigationRes graphId: Int,
                            @IdRes startDestination: Int,
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
        arguments?.let { b ->
            b.keySet().forEach { key ->
                val navArg = NavArgument.Builder()
                    .setDefaultValue(b.get(key))
                    .build()
                addArgument(key, navArg)
            }
        }
    }
    return navHostFragment
}

inline fun NavController.attachNavGraph(@NavigationRes graphId: Int, customize: NavGraph.() -> Unit) {
    val inflatedGraph = navInflater.inflate(graphId)
    inflatedGraph.customize()
    graph = inflatedGraph
}

fun Fragment.navigateUp(): Boolean {
    var popped = false
    var parent: Fragment? = this
    while (!popped) {
        popped = parent?.findNavController()?.popBackStack() ?: false
        if (!popped) {
            parent = parent?.parentFragment
            if (parent == null)
                break
        }
    }
    return popped
}

//
//fun defaultTransitionNavOptionsBuilder(): NavOptions.Builder = NavOptions.Builder()
//    .setEnterAnim(R.anim.in_from_right)
//    .setExitAnim(R.anim.out_to_left)
//    .setPopEnterAnim(R.anim.in_from_left)
//    .setPopExitAnim(R.anim.out_to_right)
//
//fun defaultTransitionNavOptions() = defaultTransitionNavOptionsBuilder().build()