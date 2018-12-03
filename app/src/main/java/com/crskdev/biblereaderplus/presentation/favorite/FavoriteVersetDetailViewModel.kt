/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.favorite

import com.crskdev.biblereaderplus.domain.entity.VersetKey
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import kotlinx.coroutines.Dispatchers

/**
 * Created by Cristian Pela on 02.12.2018.
 */
interface FavoriteVersetDetailViewModel {

    val versetKey: VersetKey

}

class FavoriteVersetDetailViewModelImpl(override val versetKey: VersetKey)
    : CoroutineScopedViewModel(Dispatchers.Main), FavoriteVersetDetailViewModel {

}