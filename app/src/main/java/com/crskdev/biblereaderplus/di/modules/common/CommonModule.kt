package com.crskdev.biblereaderplus.di.modules.common

import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import javax.inject.Singleton

/**
 * Created by Cristian Pela on 21.11.2018.
 */
@Module
class CommonModule {

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    fun provideDispatchers(): GatewayDispatchers =
        object : GatewayDispatchers {
            val customDispatcher by lazy {
                Executors.newCachedThreadPool { r ->
                    Thread(
                        r,
                        "custom-dispatching-thread"
                    )
                }.asCoroutineDispatcher()
            }
            override val IO: CoroutineDispatcher = Dispatchers.IO
            override val DEFAULT: CoroutineDispatcher = Dispatchers.Default
            override val MAIN: CoroutineDispatcher = Dispatchers.Main
            override val UNCONFINED: CoroutineDispatcher = Dispatchers.Unconfined
            override fun custom(): CoroutineDispatcher = customDispatcher
        }

}