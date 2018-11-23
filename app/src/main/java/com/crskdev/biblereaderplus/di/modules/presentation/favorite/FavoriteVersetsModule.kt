/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */
package com.crskdev.biblereaderplus.di.modules.presentation.favorite

import android.content.Context
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.di.scopes.PerFragment
import com.crskdev.biblereaderplus.domain.gateway.GatewayDispatchers
import com.crskdev.biblereaderplus.domain.interactors.favorite.FetchFavoriteVersetsInteractor
import com.crskdev.biblereaderplus.presentation.common.*
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetsFragment
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetsViewModel
import com.crskdev.biblereaderplus.presentation.favorite.FavoriteVersetsViewModelImpl
import com.crskdev.biblereaderplus.presentation.util.arch.viewModelFromProvider
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.*

/**
 * Created by Cristian Pela on 21.11.2018.
 */
@Module
abstract class FavoriteVersetsModule {

    @Module
    companion object {

        @ObsoleteCoroutinesApi
        @JvmStatic
        @PerFragment
        @Provides
        fun provideFavoriteVersetsViewModel(container: FavoriteVersetsFragment,
                                            dispatchers: GatewayDispatchers,
                                            context: Context,
                                            interactor: FetchFavoriteVersetsInteractor): FavoriteVersetsViewModel =
            viewModelFromProvider(container) {

                val transformerFactory = CharSequenceTransformerFactory(
                    EnumMap<CharSequenceTransformerFactory.Type, Lazy<CharSequenceTransformer>>(
                        CharSequenceTransformerFactory.Type::class.java
                    ).apply {
                        put(CharSequenceTransformerFactory.Type.ICON_AT_END, lazy {
                            IconAtEndTransformer(context, R.drawable.ic_star_black_10dp)
                        })
                        put(CharSequenceTransformerFactory.Type.LEAD_FIRST_LINE, lazy {
                            LeadFirstLineTransformer()
                        })
                        put(CharSequenceTransformerFactory.Type.HIGHLIGHT, lazy {
                            HighLightContentTransformer()
                        })
                    }
                )
                FavoriteVersetsViewModelImpl(
                    dispatchers.MAIN,
                    transformerFactory,
                    interactor
                )
            }

    }
}