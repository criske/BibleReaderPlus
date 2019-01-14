/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.getSystemService
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.common.util.castOrNull

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

fun AppCompatActivity.onBackPressedToExit(
    message: String = "Please click BACK again to exit",
    isUsingNavHostFragment: Boolean = false,
    delayMs: Long = 2000): Boolean {
    val fragmentManager = if (isUsingNavHostFragment) {
        supportFragmentManager
            .fragments
            .first()
            .childFragmentManager
    } else {
        supportFragmentManager
    }
    val isAboutToExit =
        !(window.decorView.tag?.castOrNull<Boolean>() ?: false)
                && (fragmentManager.backStackEntryCount == 0)
    if (isAboutToExit) {
        showSimpleToast(message)
        with(window.decorView) {
            tag = true
            postDelayed(delayMs) {
                tag = false
            }
            Unit
        }
    }
    return isAboutToExit
}

private fun traverseFragmentsRec(fragmentManager: FragmentManager, cutPoint: (Fragment) -> Unit) {
    fragmentManager.fragments.forEach {
        cutPoint(it)
        traverseFragmentsRec(it.childFragmentManager, cutPoint)
    }
}

fun AppCompatActivity.traverseFragments(cutPoint: (Fragment) -> Unit) {
    traverseFragmentsRec(supportFragmentManager, cutPoint)
}

fun Fragment.traverseFragments(cutPoint: (Fragment) -> Unit) {
    traverseFragmentsRec(childFragmentManager, cutPoint)
}


@SuppressLint("RestrictedApi")
inline fun Context.simpleInputDialog(title: String, crossinline onSubmit: (Editable) -> Unit): AlertDialog.Builder {
    val margin = 16.dpToPx(resources)
    val inputText = AppCompatEditText(withTheme(R.style.AppTheme))
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


inline fun Context.simpleAlertDialog(title: String, msg: String, crossinline onConfirm: () -> Unit): AlertDialog.Builder =
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(msg)
        .setCancelable(true)
        .setPositiveButton(android.R.string.ok) { d, _ ->
            onConfirm()
            d.dismiss()
        }

inline fun Context.showSimpleAlertDialog(title: String, msg: String, crossinline onConfirm: () -> Unit) {
    simpleAlertDialog(title, msg, onConfirm).create().show()
}

inline fun Context.simpleYesNoDialog(title: String, msg: String, crossinline onSelect: (Int) -> Unit): AlertDialog.Builder =
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(msg)
        .setCancelable(false)
        .setNegativeButton(android.R.string.no) { d, which ->
            onSelect(which)
            d.dismiss()
        }
        .setPositiveButton(android.R.string.ok) { d, which ->
            onSelect(which)
            d.dismiss()
        }

inline fun Context.showSimpleYesNoDialog(title: String, msg: String, crossinline onSelect: (Int) -> Unit) {
    simpleYesNoDialog(title, msg, onSelect).create().show()
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



