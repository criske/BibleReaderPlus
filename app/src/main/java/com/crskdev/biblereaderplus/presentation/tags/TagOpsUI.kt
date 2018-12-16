/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.tags

import android.content.Context
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.presentation.util.system.simpleAlertDialog

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
}

