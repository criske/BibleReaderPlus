/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.tags

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.presentation.common.colorpicker.ColorPickerView
import com.crskdev.biblereaderplus.presentation.util.system.dpToPx
import com.crskdev.biblereaderplus.presentation.util.system.showSimpleInputDialog
import com.crskdev.biblereaderplus.presentation.util.system.simpleAlertDialog
import com.crskdev.biblereaderplus.presentation.util.system.withTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

/**
 * Created by Cristian Pela on 15.12.2018.
 */

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
object TagOpsUI {
    inline fun showConfirmationDialogOnDelete(context: Context, tag: Tag, crossinline onConfirm: (Tag) -> Unit) {
        context.simpleAlertDialog("Warning!", "Permanently delete tag \"${tag.name}\"") {
            onConfirm(tag)
        }.setNegativeButton(android.R.string.cancel) { d, _ ->
            d.dismiss()
        }.create().show()
    }

    inline fun showRenameDialog(context: Context, tag: Tag, crossinline onConfirm: (Tag) -> Unit) {
        context.showSimpleInputDialog("Rename ${tag.name}") {
            onConfirm(tag.copy(name = it.toString()))
        }
    }

    @SuppressLint("RestrictedApi")
    inline fun showColorPicker(context: Context, tag: Tag, crossinline onConfirm: (Tag) -> Unit) {
        val margin = 16.dpToPx(context.resources)
        val colorPicker = ColorPickerView(context.withTheme(R.style.AppTheme)).apply {
            setSelectedColor(tag.color)
        }
        AlertDialog.Builder(context)
            .setTitle("Color Picker")
            .setView(colorPicker, margin, margin, margin, margin)
            .setCancelable(true).setNegativeButton(android.R.string.cancel) { d, _ ->
                d.dismiss()
            }
            .setPositiveButton(android.R.string.ok) { d, _ ->
                d.dismiss()
                val pickedColor = colorPicker.getPickedColor()
                val tagUpdated = tag.copy(color = pickedColor)
                onConfirm(tagUpdated)
            }.create().show()
    }

    fun showError(context: Context, error: TagsOpsViewModel.ErrorVM) {
        val msg = when (error) {
            TagsOpsViewModel.ErrorVM.EmptyTagName -> context.getString(R.string.err_tag_empty)
            is TagsOpsViewModel.ErrorVM.ShortTagName -> context.getString(
                R.string.err_tag_short_name,
                error.name,
                error.requiredLength
            )
            is TagsOpsViewModel.ErrorVM.Unknown -> error.err?.message
                ?: context.getString(R.string.err_unknown)
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}

