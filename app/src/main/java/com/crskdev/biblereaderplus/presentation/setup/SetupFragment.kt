/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.presentation.setup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.crskdev.biblereaderplus.R
import com.crskdev.biblereaderplus.common.util.cast
import com.crskdev.biblereaderplus.domain.entity.DeviceAccountCredential
import com.crskdev.biblereaderplus.domain.interactors.setup.SetupInteractor
import com.crskdev.biblereaderplus.presentation.awareness.IsPlatformAuthAware
import com.crskdev.biblereaderplus.presentation.util.arch.CoroutineScopedViewModel
import com.crskdev.biblereaderplus.presentation.util.arch.toChannel
import com.crskdev.biblereaderplus.presentation.util.system.showSimpleToast
import com.crskdev.biblereaderplus.presentation.util.system.showSimpleYesNoDialog
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_setup.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Cristian Pela on 05.11.2018.
 */
class SetupFragment : DaggerFragment(), IsPlatformAuthAware {

    @Inject
    lateinit var setupViewModel: SetupViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewModel.setupStepLiveData.observe(this, Observer {
            txtSetup.text = it.toString()
            when (it) {
                is SetupInteractor.Response.Error -> {
                    context?.showSimpleToast("Error ${it.errorMessage}")
                }
                is SetupInteractor.Response.SynchStep.Error -> {
                    context?.showSimpleToast("Error ${it.errMessage}")
                }
                is SetupInteractor.Response.DownloadStep.Error -> {
                    context?.showSimpleToast("Error ${it}")
                }
                is SetupInteractor.Response.Initialized -> {
                    findNavController().navigate(SetupFragmentDirections.actionSetupFragmentToReadFragment())
                }
                is SetupInteractor.Response.SynchStep.NeedPermission -> {
                    context?.showSimpleYesNoDialog("Permission", "Permission to access ACCOUNTS") {
                    }
                }
                is SetupInteractor.Response.SynchStep.Authenticating -> {
                }
                else -> setupViewModel.next()
            }
        })
    }

    override fun onPlatformAuth(resultCode: Int, data: Intent?) {
        setupViewModel.next(
            SetupInteractor
                .Request
                .AuthPromptSelection(
                    DeviceAccountCredential
                        .AuthorizationPayload(data)
                )
        )
    }
}

class SetupViewModel(private val setupInteractor: SetupInteractor) : CoroutineScopedViewModel() {

    val setupStepLiveData: LiveData<SetupInteractor.Response> =
        MutableLiveData<SetupInteractor.Response>()

    private val requestLiveData = MutableLiveData<SetupInteractor.Request>().apply {
        value = SetupInteractor.Request.Check
    }

    init {
        launch {
            requestLiveData.toChannel {
                setupInteractor.request(it) { r ->
                    setupStepLiveData.cast<MutableLiveData<SetupInteractor.Response>>().postValue(r)
                }
            }
        }
        requestLiveData.value = SetupInteractor.Request.Check
    }

    fun next(request: SetupInteractor.Request = SetupInteractor.Request.Check) {
        requestLiveData.value = request
    }

}
