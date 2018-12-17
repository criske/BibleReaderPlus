/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.tags

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.presentation.common.colorpicker.ColorPickerView
import com.crskdev.biblereaderplus.presentation.util.system.dpToPx
import com.crskdev.biblereaderplus.presentation.util.system.showSimpleInputDialog
import com.crskdev.biblereaderplus.presentation.util.system.simpleAlertDialog
import com.crskdev.biblereaderplus.presentation.util.system.withTheme

/**
 * Created by Cristian Pela on 15.12.2018.
 */

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
}

