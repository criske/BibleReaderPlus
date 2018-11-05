/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.crskdev.biblereaderplus.di.DaggerAppComponent
import com.crskdev.biblereaderplus.di.Injectable
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

/**
 * Created by Cristian Pela on 05.11.2018.
 */
class BibleReaderApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerAppComponent
            .builder()
            .application(this)
            .build()

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) = Unit
            override fun onActivityResumed(activity: Activity?) = Unit
            override fun onActivityStarted(activity: Activity?) = Unit
            override fun onActivityDestroyed(activity: Activity?) = Unit
            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) = Unit
            override fun onActivityStopped(activity: Activity?) = Unit
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                takeIf { activity is AppCompatActivity }?.let {
                    if (activity is Injectable) {
                        AndroidInjection.inject(activity)
                        activity as AppCompatActivity
                    }
                } ?: throw  IllegalArgumentException("Activity must be a support one")

            }
        })
    }

}