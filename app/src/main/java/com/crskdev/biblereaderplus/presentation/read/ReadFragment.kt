/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.presentation.read


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.presentation.favorite.VersetTransitions
import com.crskdev.biblereaderplus.presentation.util.arch.SingleLiveEvent
import com.crskdev.biblereaderplus.presentation.util.arch.distinctUntilChanged
import com.crskdev.biblereaderplus.presentation.util.system.showSimpleToast
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ReadFragment : DaggerFragment() {

    @Inject
    lateinit var viewModel: ReadViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_read, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.openLiveData.observe(this, Observer {
            when (it) {
                is ReadViewModel.Open.Verset -> findNavController().navigate(
                    ReadFragmentDirections.actionReadFragmentToFavoriteVersetDetailFragment(
                        it.navInfo.versetId,
                        it.navInfo.content,
                        it.navInfo.name
                    ),
                    FragmentNavigatorExtras(it.navInfo())
                )
                ReadViewModel.Open.Favorites -> findNavController().navigate(
                    ReadFragmentDirections.actionReadFragmentToFavoriteVersetsFragment()
                )
                ReadViewModel.Open.SearchRead -> context?.showSimpleToast("TODO()")
            }

        })
    }
}

class ReadViewModel : ViewModel() {

    val scrollReadLiveData: LiveData<ScrollData> = MutableLiveData<ScrollData>()
        .distinctUntilChanged { p, c ->
            p.readKey() != c.readKey()
        }

    val openLiveData: LiveData<Open> = SingleLiveEvent<Open>()

    fun scrollTo(source: Int, readKey: ReadKey) {
        scrollReadLiveData.cast<MutableLiveData<ScrollData>>().value = ScrollData(source, readKey)
    }

    fun open(open: Open) {
        openLiveData.cast<SingleLiveEvent<Open>>().value = open
    }

    class ScrollData(val source: Int, val readKey: ReadKey)

    sealed class Open {
        class Verset(val navInfo: VersetTransitions.NavInfoExtra) : Open()
        object Favorites : Open()
        object SearchRead : Open()
    }
}

