/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.Term
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TermTest {
    private lateinit var term1: Term
    private lateinit var term2: Term

    @Before
    fun setup() {
        term1 = Term()
        term2 = Term()
    }

    @Test
    fun compareTo_Equal() {
        val t1 = term1.copy(isGroupTerm = true)
        val t2 = term2.copy(isGroupTerm = true)

        assertEquals(0, t1.compareTo(t2).toLong())
    }

    @Test
    fun compareTo_After() {
        val t1 = term1.copy(isGroupTerm = true)
        val t2 = term2.copy(isGroupTerm = false)

        assertEquals(1, t1.compareTo(t2).toLong())
    }

    @Test
    fun compareTo_Before() {
        val t1 = term1.copy(isGroupTerm = false)
        val t2 = term2.copy(isGroupTerm = true)

        assertEquals(-1, t1.compareTo(t2).toLong())
    }

    @Test
    fun getEndAt_TestNullEndDate() {
        val t1 = term1.copy(endAt = null)

        assertEquals(null, t1.endDate)
    }

    @Test
    fun getStartAt_TestNullStartDate() {
        val t1 = term1.copy(startAt = null)

        assertEquals(null, t1.startDate)
    }
}