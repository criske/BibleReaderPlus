/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.tags

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.domain.entity.Tag
import com.crskdev.biblereaderplus.domain.entity.TagOp
import com.crskdev.biblereaderplus.domain.interactors.tag.FetchTagsInteractor
import com.crskdev.biblereaderplus.domain.interactors.tag.TagOpsInteractor
import com.crskdev.biblereaderplus.domain.interactors.tag.TagOpsInteractor.ResponseError
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.SingleLiveEvent
import com.crskdev.biblereaderplus.presentation.util.arch.interval
import com.crskdev.biblereaderplus.presentation.util.arch.toChannel
import com.crskdev.biblereaderplus.presentation.util.system.dpToPx
import com.crskdev.biblereaderplus.presentation.util.system.showSimpleToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.tag_search_view_layout.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Cristian Pela on 14.12.2018.
 */
@ExperimentalCoroutinesApi
class TagsSearchBottomSheetDialogFragment : BottomSheetDialogFragment(),
    HasSupportFragmentInjector {

    companion object {
        fun show(fragmentManager: FragmentManager) {
            TagsSearchBottomSheetDialogFragment().show(
                fragmentManager,
                TagsSearchBottomSheetDialogFragment::class.java.simpleName
            )
        }
    }

    @Inject
    lateinit var childFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var tagsOpsViewModel: TagsOpsViewModel

    @Inject
    lateinit var tagSelectViewModel: TagSelectViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tag_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tagsSearchView = view as TagsSearchView
        with(tagsSearchView) {
            onSearchListener {
                when (it) {
                    is TagsSearchView.Action.Query -> {
                        tagsOpsViewModel.searchTagsWith(it.query)
                    }
                    is TagsSearchView.Action.Select -> {
                        tagSelectViewModel.selectedTagLiveData.value = it.tag
                    }
                    is TagsSearchView.Action.Create -> {
                        tagsOpsViewModel.createTag(it.tagName)
                    }
                    is TagsSearchView.Action.Rename -> {
                        tagsOpsViewModel.renameTag(it.tag.id, it.tag.name)
                    }
                }
            }
            recyclerTagSearch.layoutParams = recyclerTagSearch.layoutParams
                .apply {
                    height = (300).dpToPx(resources)
                }
            post {
                setQuery("")
            }
            Unit
        }
        tagsOpsViewModel.searchTagsLiveData.observe(this, Observer {
            tagsSearchView.submitSuggestions(it)
        })
        tagsOpsViewModel.errorsLiveData.observe(this, Observer {
            context?.showSimpleToast("${it.javaClass.simpleName}: ${it.err ?: ""}")
        })
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = childFragmentInjector

}


@ExperimentalCoroutinesApi
class TagsOpsViewModel(
    private val fetchTagsInteractor: FetchTagsInteractor,
    private val tagOpsInteractor: TagOpsInteractor
) : CoroutineScopedViewModel() {

    sealed class ErrorVM(val err: Throwable?) {
        object EmptyTagName : ErrorVM(null)
        object ShortTagName : ErrorVM(null)
        class Unknown(err: Throwable?) : ErrorVM(err)
    }

    private val querySearchTagsLiveData: MutableLiveData<String?> = MutableLiveData()

    val searchTagsLiveData: SingleLiveEvent<List<Tag>> = SingleLiveEvent()

    val errorsLiveData: SingleLiveEvent<ErrorVM> = SingleLiveEvent()

    private val errorHandler: (ResponseError) -> Unit = {
        val err = when (it) {
            is ResponseError.EmptyTagName -> ErrorVM.EmptyTagName
            is ResponseError.ShortTagName -> ErrorVM.ShortTagName
            is ResponseError.Unknown -> ErrorVM.Unknown(it.err)
        }
        errorsLiveData.postValue(err)
    }

    init {
        launch {
            querySearchTagsLiveData.interval(300, TimeUnit.MILLISECONDS).toChannel {
                fetchTagsInteractor.request(it) {
                    searchTagsLiveData.cast<MutableLiveData<List<Tag>>>()
                        .postValue(it.toList())
                }
            }
        }
    }

    fun searchTagsWith(name: String) {
        querySearchTagsLiveData.value = name
    }

    fun createTag(tagName: String) {
        launch {
            tagOpsInteractor.request(
                TagOp.Create(tagName),
                errorHandler
            )
        }
    }

    fun renameTag(id: String, newName: String) {
        launch {
            tagOpsInteractor.request(
                TagOp.Rename(id, newName),
                errorHandler
            )
        }
    }

}


class TagSelectViewModel : ViewModel() {

    val selectedTagLiveData: SingleLiveEvent<Tag> = SingleLiveEvent()
}