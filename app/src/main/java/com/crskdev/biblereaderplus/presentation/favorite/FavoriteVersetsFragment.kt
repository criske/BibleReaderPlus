package com.crskdev.biblereaderplus.presentation.favorite


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_search_favorite.*
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class FavoriteVersetsFragment : DaggerFragment() {

    @Inject
    lateinit var viewModel: FavoriteVersetsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        favHello.text = viewModel.hello
    }

}

class FavoriteVersetsViewModel(mainDispatcher: CoroutineDispatcher) :
    CoroutineScopedViewModel(mainDispatcher) {


    val hello = "Hello"
}
