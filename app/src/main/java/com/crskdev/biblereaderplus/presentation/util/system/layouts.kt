/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.system

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Cristian Pela on 01.11.2018.
 */
fun LayoutInflater.prepInflate(parent: ViewGroup, attachToRoot: Boolean = false): (Int) -> View = {
    inflate(it, parent, attachToRoot)
}
