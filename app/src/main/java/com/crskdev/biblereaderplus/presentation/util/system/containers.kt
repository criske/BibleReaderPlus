/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.system

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
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

fun Activity.hideSoftKeyboard() {
    val view = currentFocus ?: View(this);
    getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(view.windowToken, 0);
}

/**
 * useful to obtain parcelable decendentats of an unparcelable class (like abstract or sealed)
 * ``
 *
 *     sealed class Foo{
 *          @Parcelize
 *          class A: Foo(), Parcelable {}
 * }
 *
 *      bundle.getParcelableMixin<Foo>(KEY)
 * ``
 */
inline fun <reified T> Bundle.getParcelableMixin(key: String): T? =
    this.getParcelable<Parcelable>(key)?.cast<T>()



