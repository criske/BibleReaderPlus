/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.util.arch

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * Created by Cristian Pela on 30.11.2018.
 */
//fun <T:Lifecycle> LifecycleOwner.lifecycleAware():T{
//
//}
//
//
//operator fun <T: Lifecycle> T.getValue(thisRef: Any?, property: KProperty<*>): T{
//    if(thisRef is LifecycleOwner){
//
//    }else{
//        throw IllegalArgumentException("Delegation reference must be a LifeCycleOwner")
//    }
//}

class ViewLifecycleOwner(view: View) : LifecycleOwner {

    private var registry: LifecycleRegistry = LifecycleRegistry(this)

    init {
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            override fun onViewDetachedFromWindow(v: View?) {
                registry.markState(Lifecycle.State.DESTROYED)
            }

            override fun onViewAttachedToWindow(v: View?) {
                registry.markState(Lifecycle.State.CREATED)
                registry.markState(Lifecycle.State.STARTED)
            }

        })
    }


    override fun getLifecycle(): Lifecycle = registry

}