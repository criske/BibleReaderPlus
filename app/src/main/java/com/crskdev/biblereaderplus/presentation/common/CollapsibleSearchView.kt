/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.common

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.castIf
import com.crskdev.biblereaderplus.presentation.util.system.dpToPx
import com.crskdev.biblereaderplus.presentation.util.system.getColorCompat

/**
 * Created by Cristian Pela on 04.12.2018.
 */
class CollapsibleSearchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SearchView(context, attrs, defStyleAttr) {

    override fun onActionViewExpanded() {
        super.onActionViewExpanded()
        parent.castIf<Toolbar>()?.let { toolbar ->
            setBackgroundColor(context.getColorCompat(R.color.backgroundColor))
            toolbar.menu.forEach {
                it.isEnabled = false
            }
            post {
                val lp = layoutParams as Toolbar.LayoutParams
                lp.width = toolbar.let {
                    width - paddingEnd - paddingStart + it.menu.size() * (28 + 2).dpToPx(resources)
                }
                lp.gravity = Gravity.CENTER_VERTICAL
                layoutParams = lp
                requestLayout()
            }
        }
    }

    override fun onActionViewCollapsed() {
        super.onActionViewCollapsed()
        parent.castIf<Toolbar>()?.menu?.forEach {
            it.isEnabled = true
        }
    }

}