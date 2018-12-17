/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.tags

import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.presentation.util.system.dpToPx
import com.crskdev.biblereaderplus.presentation.util.system.withTheme
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.tag_search_view_layout.view.*

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
                            TagsSearchView(context.withTheme(R.style.AppTheme))
                                .apply {
                                    onSearchListener(listener)
                                    val sheetHeight = 400
                                    setContentView(
                                        this, ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            sheetHeight.dpToPx(resources)
                                        )
                                    )
                                    recyclerTagSearch.layoutParams =
                                            recyclerTagSearch.layoutParams.apply {
                                                height = (sheetHeight - 65).dpToPx(resources)
                                            }
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