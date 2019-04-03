/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.student.holders.QuizNumericalViewHolder
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuizUtilsTest : TestCase() {

    @Test
    fun `Numerical question ignores trailing decimal point`() {
        val input = "5."
        val expected = "5"
        val actual = QuizNumericalViewHolder.sanitizeInput(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `Numerical question drops trailing zeros`() {
        val input = "5.123000"
        val expected = "5.123"
        val actual = QuizNumericalViewHolder.sanitizeInput(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `Numerical question corrects missing leading zero`() {
        val input = ".5"
        val expected = "0.5"
        val actual = QuizNumericalViewHolder.sanitizeInput(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `Numerical question trims leading zeros`() {
        val input = "00500"
        val expected = "500"
        val actual = QuizNumericalViewHolder.sanitizeInput(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `Numerical question does not modify valid input`() {
        val validInputs = listOf(
            "28.6666",
            "1",
            "0",
            "100000.00001"
        )
        validInputs.forEach { assertEquals(it, QuizNumericalViewHolder.sanitizeInput(it)) }
    }

}
