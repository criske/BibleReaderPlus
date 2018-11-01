/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.math.MathUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import kotlin.math.roundToInt

class ReadFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_read, container, false)
    }

}

internal class ReadViewModel : ViewModel() {

    val scrollReadLiveData: LiveData<ReadKey> = MutableLiveData()

    fun scrollTo(readKey: ReadKey) {
        scrollReadLiveData.cast<MutableLiveData<ReadKey>>().value = readKey
    }

}


object RangeNormalizer01 {

    fun normalize(max: Int, unNormalized: Int): Float {
        val normalized = (unNormalized - 0f) / (max - 0f)
        return MathUtils.clamp(normalized, 0f, 1f)
    }

    fun deNormalize(max: Int, normalized: Float): Int = (normalized * max).roundToInt()
}

