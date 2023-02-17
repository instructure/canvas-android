/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.test

import com.emeritus.student.mobius.common.LateInit
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

@Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")
class LateInitTest : Assert() {

    @Test
    fun `Invokes init block on initialization`() {
        val testValue = "test"
        val block = mockk<(String) -> String>(relaxed = true)
        var prop: String by LateInit(block)
        prop = testValue
        verify { block.invoke(testValue) }
        confirmVerified(block)
    }

    @Test
    fun `Sets init block return value on initialization`() {
        val testValue = "test"
        val expectedValue = testValue.reversed()
        var prop: String by LateInit { it.reversed() }
        prop = testValue
        assertEquals(expectedValue, prop)
    }

    @Test
    fun `Invokes init block only once`() {
        val testValue = "test"
        val block = mockk<(String) -> String>(relaxed = true)
        var prop: String by LateInit(block)
        repeat(3) { prop = testValue }
        verify(exactly = 1) { block.invoke(testValue) }
        confirmVerified(block)
    }

    @Test
    fun `Sets value without modification after initialization`() {
        val testValue = "test"
        val expectedValue = "abc123"
        var prop: String by LateInit { it }
        prop = testValue // Initialization
        prop = expectedValue
        assertEquals(expectedValue, prop)
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun `Throws exception when accessed before initialization`() {
        val prop: String by LateInit { "" }
        println(prop)
    }
}
