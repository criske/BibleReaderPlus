/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.presentation.common.TagsSearchView
import com.crskdev.biblereaderplus.presentation.util.system.dpToPx
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Created by Cristian Pela on 30.11.2018.
 */
class TagsSearchBottomSheetDialogHelper(private val context: Context,
                                        private val listener: (TagsSearchView.Action) -> Unit) :
    LifecycleObserver {

    private var sheetDialog: BottomSheetDialog? = null

    private var tagsSearchView: TagsSearchView? = null

    private var isDialogCreated = false

    fun toggleBottomSheet() {
        if (sheetDialog == null || sheetDialog?.isShowing == false) {
            displayBottomSheet()
        } else {
            sheetDialog?.dismiss()
        }
    }

    fun displayBottomSheet() {
        if (sheetDialog == null) {
            sheetDialog = BottomSheetDialog(context)
                .apply {
                    setCancelable(true)
                    setCanceledOnTouchOutside(true)
                    setOnCancelListener {
                        dismiss()
                    }
                    tagsSearchView =
                            TagsSearchView(ContextThemeWrapper(context, R.style.AppTheme)).apply {
                                onSearchListener(listener)
                                setContentView(
                                    this, ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        300.dpToPx(context.resources)
                                    )
                                )
                                post {
                                    setQuery("")
                                }
                            }
                }
        }
        sheetDialog?.show()
    }

    fun submitSuggestions(tags: List<Tag>) {
        tagsSearchView?.submitSuggestions(tags)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clear() {
        sheetDialog?.dismiss()
        sheetDialog = null
        tagsSearchView = null
        isDialogCreated = false
    }

}