/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.system

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.crskdev.biblereaderplus.common.util.cast

/**
 * Created by Cristian Pela on 01.11.2018.
 */
fun LayoutInflater.prepInflate(parent: ViewGroup, attachToRoot: Boolean = false): (Int) -> View = {
    inflate(it, parent, attachToRoot)
}

fun Activity.hideSoftKeyboard(lostFocusView: View? = null) {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE).cast<InputMethodManager>()
    val view = lostFocusView ?: currentFocus ?: View(this)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}
