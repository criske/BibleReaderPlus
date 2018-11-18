/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.domain.gateway

/**
 * Created by Cristian Pela on 06.11.2018.
 */
interface AuthService {

    fun isAuthenticated(): Boolean

    fun hasPermission(): Boolean

    fun requestPermission()

    fun authenticate(deviceAccountCredentials: Any): Pair<Error?, Boolean>

    fun authenticateWithPermissionGranted(): Pair<Error?, Boolean>

}