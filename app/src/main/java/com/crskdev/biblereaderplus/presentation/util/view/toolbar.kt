/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.view

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.iterator
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.presentation.common.CollapsibleSearchView
import com.crskdev.biblereaderplus.presentation.util.system.dpToPx
import com.crskdev.biblereaderplus.presentation.util.system.withTheme

/**
 * Created by Cristian Pela on 26.11.2018.
 */
fun Toolbar.tintIcon(menuItemId: Int, @ColorRes color: Int = android.R.color.darker_gray) {
    post {
        val c = ContextCompat.getColor(context, color)
        menu.findItem(menuItemId)?.apply {
            this.icon = DrawableCompat.wrap(this.icon)?.mutate()?.apply {
                DrawableCompat.setTint(this, c)
            }
        }
    }
}

fun Toolbar.tintIcons(@ColorRes color: Int = android.R.color.darker_gray) {
    post {
        val c = ContextCompat.getColor(context, color)
        menu.iterator().forEach {
            it.icon = DrawableCompat.wrap(it.icon)?.mutate()?.apply {
                DrawableCompat.setTint(this, c)
            }
        }
        navigationIcon?.let { DrawableCompat.setTint(DrawableCompat.wrap(it), c) }
    }
}

inline fun Toolbar.setup(@MenuRes menu: Int? = null, @ColorRes iconsTint: Int = android.R.color.darker_gray,
                         crossinline menuItemListener: (MenuItem) -> Boolean = { true }) {
    menu?.let {
        inflateMenu(it)
    }
    tintIcons(iconsTint)
    setOnMenuItemClickListener {
        menuItemListener(it)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        elevation = 5.dpToPx(resources).toFloat()
    }
}

const val ADDED_SEARCH_ID = 1337

inline fun Menu.addSearch(context: Context, @StringRes title: Int, expandedByDefault: Boolean = false,
                          crossinline onChange: (String) -> Unit = {},
                          crossinline onClose: () -> Unit = {},
                          crossinline onClear: () -> Unit = {},
                          crossinline onSubmit: (String) -> Unit = {}) {

    add(Menu.NONE, ADDED_SEARCH_ID, 10000, title).apply {
        actionView = CollapsibleSearchView(context.withTheme(R.style.AppTheme)).apply {
            setIconifiedByDefault(false)
            val sv = this
            setOnCloseListener {
                onClose()
                true
            }
            findViewById<ImageView>(R.id.search_mag_icon).apply {
                setImageResource(R.drawable.ic_search_black_24dp)
                setColorFilter(Color.GRAY)
            }
            findViewById<ImageView>(R.id.search_close_btn).apply {
                setImageResource(R.drawable.ic_clear_black_24dp)
                setColorFilter(Color.GRAY)
                setOnClickListener {
                    onClear()
                }
            }
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    onSubmit(query)
                    sv.clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    onChange(newText)
                    return true
                }
            })
        }
        icon = ContextCompat.getDrawable(context, R.drawable.ic_search_black_24dp)
        setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW.or(MenuItem.SHOW_AS_ACTION_ALWAYS))
        if (expandedByDefault) {
            expandActionView()
        }
    }
}

inline fun <reified V : View> Menu.findActionView(@IdRes itemId: Int): V =
    findItem(itemId).actionView as V