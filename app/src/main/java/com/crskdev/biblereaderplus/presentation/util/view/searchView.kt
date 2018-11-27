/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.view

import android.widget.AutoCompleteTextView
import androidx.appcompat.widget.SearchView


/**
 * Created by Cristian Pela on 27.11.2018.
 */
fun SearchView.autocompleteTextView(): AutoCompleteTextView =
    findViewById(androidx.appcompat.R.id.search_src_text);