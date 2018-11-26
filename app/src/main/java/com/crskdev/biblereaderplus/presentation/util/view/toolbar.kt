/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.view

import android.content.Context
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.ColorRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.iterator
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.presentation.util.system.dpToPx

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            elevation = 5.dpToPx(resources).toFloat()
        }
    }
}

inline fun Toolbar.inflateTintedMenu(@MenuRes menu: Int, @ColorRes color: Int = android.R.color.darker_gray,
                                     crossinline menuItemListener: (MenuItem) -> Boolean = { true }) {
    inflateMenu(menu)
    tintIcons(color)
    setOnMenuItemClickListener {
        menuItemListener(it)
    }
}

inline fun Menu.addSearch(context: Context, @StringRes title: Int, expandedByDefault: Boolean = false,
                          crossinline onChange: (String) -> Unit = {},
                          crossinline onSubmit: (String) -> Unit = {}) {

    add(title).apply {
        actionView = SearchView(
            ContextThemeWrapper(
                context,
                R.style.ThemeOverlay_MaterialComponents_Light_TintedIcon
            )
        )
            .apply {
                maxWidth = Int.MAX_VALUE
                setIconifiedByDefault(false)
                val sv = this
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