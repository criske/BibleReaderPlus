/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2020.
 */

package com.crskdev.biblereaderplus.services

import android.app.Activity
import android.content.Intent
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.domain.gateway.AuthService
import com.crskdev.biblereaderplus.domain.gateway.AuthService.Companion.PLATFORM_SIGNIN_REQUEST_CODE
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Created by Cristian Pela on 07.01.2019.
 */
class AuthServiceImpl(private val activity: Activity) : AuthService {

    companion object {
        private const val WEB_CLIENT_ID =
            "908882117263-ucoi1vdg59ajvnhemqq4r8uqeck5k6hu.apps.googleusercontent.com"
    }

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(activity, gso)
    }

    override fun isAuthenticated(): Boolean = auth.currentUser != null

    override fun isTokenExpired(): Boolean {
        TODO()
    }

    override fun hasAccountPermissionGranted(): Boolean =
        GoogleSignIn.getLastSignedInAccount(activity.applicationContext) != null

    override fun hasPermission(): Boolean = true

    override fun requestPermission() {}

    override fun requestAuthPermission() {
        activity.startActivityForResult(
            googleSignInClient.signInIntent,
            PLATFORM_SIGNIN_REQUEST_CODE
        )
    }

    override fun authenticate(deviceAccountCredentials: Any?): Pair<Error?, Boolean> {
        val intent = try {
            requireNotNull(deviceAccountCredentials).cast<Intent>()
        } catch (e: Exception) {
            return java.lang.Error(e) to false
        }
        val signInTask = GoogleSignIn.getSignedInAccountFromIntent(intent)
        try {
            val account = Tasks.await(signInTask)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Tasks.await(auth.signInWithCredential(credential))
        } catch (e: Exception) {
            return java.lang.Error(e) to false
        }
        return null to true
    }

    override fun authenticateWithPermissionGranted(): Pair<Error?, Boolean> {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(activity.applicationContext)
                ?: throw Exception("Account not found")
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Tasks.await(auth.signInWithCredential(credential))

        } catch (e: Exception) {
            return java.lang.Error(e) to false
        }
        return null to true
    }


}