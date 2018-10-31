/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

@file:Suppress("unused")

package com.crskdev.biblereaderplus.presentation.util.arch

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.crskdev.biblereaderplus.common.util.NowTimeProvider
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Cristian Pela on 31.10.2018.
 */
inline fun <T> LiveData<T>.filter(crossinline predicate: (T?) -> Boolean): LiveData<T> =
    MediatorLiveData<T>().apply {
        addSource(this@filter) {
            if (predicate(it)) {
                value = it
            }
        }
    }


fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<T> {
        var lastValue: T? = null
        override fun onChanged(t: T) {
            if (lastValue != t) {
                mutableLiveData.value = t
                lastValue = t
            }
        }
    })
    return mutableLiveData
}

inline fun <T> LiveData<T>.distinctUntilChanged(crossinline predicate: (T, T) -> Boolean): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<T> {
        var lastValue: T? = null
        override fun onChanged(t: T) {
            val prevT = lastValue
            if (prevT == null || predicate(prevT, t)) {
                mutableLiveData.value = t
                lastValue = t
            }
        }
    })
    return mutableLiveData
}

fun <T> LiveData<T>.skip(count: Int): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<T> {
        var valueCount = 0
        override fun onChanged(t: T) {
            if (valueCount >= count) {
                mutableLiveData.value = t
            }
            valueCount++
        }
    })
    return mutableLiveData
}

fun <T> LiveData<T>.skipFirst(): LiveData<T> = skip(1)

fun <T> LiveData<T>.interval(
    duration: Long, unit: TimeUnit, nowTimeProvider: NowTimeProvider = object :
        NowTimeProvider {}
): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<T> {
        var lastTime = 0L
        val intervalMillis = unit.toMillis(duration)

        override fun onChanged(t: T) {
            val now = nowTimeProvider.now()
            val delta = now - lastTime
            if (delta > intervalMillis) {
                mutableLiveData.value = t
                lastTime = now
            }
        }
    })
    return mutableLiveData
}

fun <T> LiveData<T>.interval(itemThreshold: Int): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<T> {
        var emitted = 0
        override fun onChanged(t: T) {
            if (emitted >= itemThreshold) {
                mutableLiveData.value = t
                emitted = 0
            }
            emitted += 1
        }
    })
    return mutableLiveData
}

fun <T> merge(list: List<LiveData<T>>): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    list.forEach { ld ->
        mutableLiveData.addSource(ld) {
            mutableLiveData.value = it
        }
    }
    return mutableLiveData
}


inline fun <T, R> LiveData<T>.splitAndMerge(block: LiveData<T>.() -> List<LiveData<R>>): LiveData<R> {
    return merge(this.block())
}

inline fun <T, R> LiveData<T>.scan(initialValue: R, crossinline mapper: (R, T) -> R): LiveData<R> {
    val mutableLiveData: MediatorLiveData<R> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<T> {
        var accValue = initialValue
        override fun onChanged(t: T) {
            accValue = mapper(accValue, t)
            mutableLiveData.value = accValue
        }
    })
    return mutableLiveData
}

inline fun <T> LiveData<T>.onNext(crossinline block: (T) -> Unit): LiveData<T> {
    val mutableLiveData: MediatorLiveData<T> = MediatorLiveData()
    mutableLiveData.addSource(this) { t ->
        mutableLiveData.value = t
        block(t)
    }
    return mutableLiveData
}

@Suppress("UNCHECKED_CAST")
inline fun <reified R : Any> LiveData<*>.castLD(): LiveData<R> {
    val mutableLiveData: MediatorLiveData<R> = MediatorLiveData()
    mutableLiveData.addSource(this, object : Observer<Any> {
        override fun onChanged(item: Any) {
            mutableLiveData.value = item as R
        }
    })
    return mutableLiveData
}

fun <T, V> LiveData<T>.switchMap(block: (T) -> LiveData<V>): LiveData<V> =
    Transformations.switchMap(this, block)

fun <T, V> LiveData<T>.map(block: (T) -> V): LiveData<V> =
    Transformations.map(this, block)

fun <T> empty() = MutableLiveData<T>()

fun <T> just(item: T) = MutableLiveData<T>().apply {
    value = item
}


class SingleLiveEvent<T> : MutableLiveData<T>() {

    private val mPending = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
        }
        // Observe the internal MutableLiveData
        super.observe(owner, Observer { t ->
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(t: T?) {
        mPending.set(true)
        super.setValue(t)
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        value = null
    }

    fun toNonSingleLiveData(): LiveData<T> {
        val liveData = MediatorLiveData<T>()
        liveData.addSource(this) { t -> liveData.value = t }
        return liveData
    }

    companion object {
        private val TAG = "SingleLiveEvent"
    }
}

