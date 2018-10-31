/*
 * License: MIT
 * Copyright (c)  Pela Cristian 2018.
 */

package com.crskdev.biblereaderplus.presentation.read

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by Cristian Pela on 31.10.2018.
 */
class RangeNormalizerTest {

    @Test
    fun next() {
        val deviation = .0005f
        assertEquals(0.5f, RangeNormalizer01.normalize(10, 5), deviation)
        assertEquals(1f, RangeNormalizer01.normalize(10, 20), deviation)
        assertEquals(0f, RangeNormalizer01.normalize(10, -20), deviation)
        assertEquals(0.2f, RangeNormalizer01.normalize(10, 2), deviation)
    }
}