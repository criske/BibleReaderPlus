/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.arch

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor

/**
 * Created by Cristian Pela on 13.12.2018.
 */
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
suspend fun <T> LiveData<T>.toChannel(mainDispatcher: CoroutineDispatcher = Dispatchers.Main, block: suspend (ReceiveChannel<T>) -> Unit) =
    coroutineScope {
        val sendChannel = actor<T> {
            block(channel)
        }
        val observer = Observer<T> {
            launch {
                sendChannel.send(it)
            }
        }
        launch(mainDispatcher) {
            observeForever(observer)
        }
        sendChannel.invokeOnClose {
            this@toChannel.removeObserver(observer)
        }
    }