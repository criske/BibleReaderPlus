/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.system

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.getSystemService
import com.crskdev.biblereaderplus.R
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

@SuppressLint("RestrictedApi")
inline fun Context.simpleInputDialog(title: String, crossinline onSubmit: (Editable) -> Unit): AlertDialog.Builder {
    val margin = 16.dpToPx(resources)
    val inputText = AppCompatEditText(ContextThemeWrapper(this, R.style.AppTheme))
        .apply {
            setSingleLine()
        }
    return AlertDialog.Builder(this)
        .setTitle(title)
        .setView(inputText, margin, margin, margin, margin)
        .setCancelable(true)
        .setPositiveButton(android.R.string.ok) { d, _ ->
            d.dismiss()
            inputText.text?.let { onSubmit(it) }
        }
}

inline fun Context.showSimpleInputDialog(title: String, crossinline onSubmit: (Editable) -> Unit) {
    simpleInputDialog(title, onSubmit).create().show()
}

fun Context.showSimpleAlertDialog(title: String) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setCancelable(true)
        .setPositiveButton(android.R.string.ok) { d, _ ->
            d.dismiss()
        }.create().show()
}

fun Context.showSimpleToast(title: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, title, duration).show()
}

/**
 * useful to obtain parcelable descendants of an unparcelable class (like abstract or sealed)
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



