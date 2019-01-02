/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import android.view.View

/**
 * Created by Cristian Pela on 02.01.2019.
 */
object VersetTransitions {
    fun name(versetId: Int): String = "verset-detail-transition-name-$versetId"
    fun navInfoExtra(versetId: Int, view: View, content: String): NavInfoExtra =
        NavInfoExtra(versetId, view, name(versetId), content)

    class NavInfoExtra(val versetId: Int,
                       val view: View,
                       val name: String,
                       val content: String) {
        operator fun invoke(): Pair<View, String> = view to name
    }
}