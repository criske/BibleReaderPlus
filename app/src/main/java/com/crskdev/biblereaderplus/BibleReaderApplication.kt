/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.crskdev.biblereaderplus.common.util.castIf
import com.crskdev.biblereaderplus.di.DaggerAppComponent
import com.crskdev.biblereaderplus.di.Injectable
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.support.AndroidSupportInjection

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
        registerActivityLifecycleCallbacks(DiActivityLifeCycleCallbacks())
    }

}

class DiActivityLifeCycleCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity?) = Unit
    override fun onActivityResumed(activity: Activity?) = Unit
    override fun onActivityStarted(activity: Activity?) = Unit
    override fun onActivityDestroyed(activity: Activity?) = Unit
    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) = Unit
    override fun onActivityStopped(activity: Activity?) = Unit
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activity.castIf<AppCompatActivity>()
            ?.apply {
                if (this is Injectable) {
                    AndroidInjection.inject(activity)
                }
                supportFragmentManager.registerFragmentLifecycleCallbacks(object :
                    FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                        if (f is Injectable) {
                            AndroidSupportInjection.inject(f)
                        }
                    }
                }, true)
            }
            ?: throw  IllegalArgumentException("Activity must be a support one")
    }
}
