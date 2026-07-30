/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.grades

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatIfScoreInputValidationTest {

    @Test
    fun `Empty input is valid with a null score`() {
        val result = validateWhatIfScoreInput("", maxScore = 100.0)

        assertEquals("", result?.text)
        assertNull(result?.score)
        assertFalse(result!!.isOverMax)
    }

    @Test
    fun `Integer input parses to a score with no over-max warning when under max`() {
        val result = validateWhatIfScoreInput("85", maxScore = 100.0)

        assertEquals(85.0, result?.score)
        assertFalse(result!!.isOverMax)
    }

    @Test
    fun `Single-decimal input is accepted`() {
        val result = validateWhatIfScoreInput("85.5", maxScore = 100.0)

        assertEquals(85.5, result?.score)
        assertFalse(result!!.isOverMax)
    }

    @Test
    fun `Two-decimal input is accepted`() {
        val result = validateWhatIfScoreInput("85.25", maxScore = 100.0)

        assertEquals(85.25, result?.score)
        assertFalse(result!!.isOverMax)
    }

    @Test
    fun `Three-decimal input is rejected`() {
        val result = validateWhatIfScoreInput("85.123", maxScore = 100.0)

        assertNull(result)
    }

    @Test
    fun `Non-numeric input is rejected`() {
        val result = validateWhatIfScoreInput("abc", maxScore = 100.0)

        assertNull(result)
    }

    @Test
    fun `Score over max sets the over-max warning`() {
        val result = validateWhatIfScoreInput("150", maxScore = 100.0)

        assertEquals(150.0, result?.score)
        assertTrue(result!!.isOverMax)
    }

    @Test
    fun `Score over a zero max still sets the over-max warning`() {
        val result = validateWhatIfScoreInput("5", maxScore = 0.0)

        assertEquals(5.0, result?.score)
        assertTrue(result!!.isOverMax)
    }

    @Test
    fun `Score within max has no over-max warning`() {
        val result = validateWhatIfScoreInput("50", maxScore = 100.0)

        assertEquals(50.0, result?.score)
        assertFalse(result!!.isOverMax)
    }
}
