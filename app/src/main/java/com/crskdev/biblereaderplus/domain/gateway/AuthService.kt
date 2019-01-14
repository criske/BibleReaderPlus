/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.gateway

/**
 * Created by Cristian Pela on 06.11.2018.
 */
interface AuthService {
    companion object {
        val PLATFORM_SIGNIN_REQUEST_CODE = 1337
    }

    fun isAuthenticated(): Boolean

    fun hasPermission(): Boolean

    fun requestPermission()

    fun authenticate(deviceAccountCredentials: Any?): Pair<Error?, Boolean>

    fun authenticateWithPermissionGranted(): Pair<Error?, Boolean>

    fun requestAuthPermission()

    fun hasAccountPermissionGranted(): Boolean
}