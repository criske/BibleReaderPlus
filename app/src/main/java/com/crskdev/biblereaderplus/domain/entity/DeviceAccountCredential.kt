/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.entity

/**
 * Created by Cristian Pela on 07.11.2018.
 */
sealed class DeviceAccountCredential {
    object Unauthorized : DeviceAccountCredential()
    class AuthorizationPayload(val credentialData: Any?) : DeviceAccountCredential()
}