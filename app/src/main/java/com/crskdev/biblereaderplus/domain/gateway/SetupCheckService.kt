/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2019.
 */

package com.crskdev.biblereaderplus.domain.gateway

import kotlin.math.sign

/**
 * Created by Cristian Pela on 06.11.2018.
 */
interface SetupCheckService {

    fun getStep(): Step

    fun save(step: Step)

//    sealed class StepDeprecated {
//        object Initialized : StepDeprecated()
//        object Uninitialized : StepDeprecated()
//
//        object DownloadStep : StepDeprecated()
//        object AuthStep : StepDeprecated()
//        object SynchStep : StepDeprecated()
//    }

    enum class Step {

        UNINITIALIZED, DOWNLOAD, AUTH, SYNCH, INITIALIZED;

        companion object {
            fun next(step: Step): Step = dir(step, 1)
            fun previous(step: Step): Step = dir(step, -1)
            private fun dir(step: Step, sign: Int): Step {
                val values = Step.values()
                val pos = (step.ordinal + sign.sign) % values.size
                return values[pos]
            }
        }
    }

}

fun SetupCheckService.Step.next() = SetupCheckService.Step.next(this)
fun SetupCheckService.Step.previous() = SetupCheckService.Step.previous(this)