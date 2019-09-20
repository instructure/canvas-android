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
 */
package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import org.junit.Assert
import org.junit.Test

class DataResultTest : Assert() {

    @Test
    fun `Executes onFail block if type matches`() {
        var executed = false
        val result = DataResult.Fail(Failure.Authorization())
        result.onFail<Failure.Authorization> {
            executed = true
        }
        assertTrue(executed)
    }

    @Test
    fun `Does not execute onFail block if type does not match`() {
        var executed = false
        val result = DataResult.Fail(Failure.Network())
        result.onFail<Failure.Authorization> {
            executed = true
        }
        assertFalse(executed)
    }

    @Test
    fun `Does not execute onFail block if successful`() {
        var executed = false
        val result = DataResult.Success("Success")
        result.onFail<Failure.Authorization> {
            executed = true
        }
        assertFalse(executed)
    }

    @Test
    fun `Executes onFailure block if failed`() {
        var executed = false
        val result = DataResult.Fail(Failure.Authorization())
        result.onFailure { executed = true }
        assertTrue(executed)
    }

    @Test
    fun `Does not execute onFailure block if successful`() {
        var executed = false
        val result = DataResult.Success("Success")
        result.onFailure { executed = true }
        assertFalse(executed)
    }

    @Test
    fun `Executes success block if successful`() {
        var executed = false
        val result = DataResult.Success("Success")
        result.onSuccess {
            executed = true
        }
        assertTrue(executed)
    }

    @Test
    fun `isSuccess is true and isFail is false if successful`() {
        val result = DataResult.Success("Success")
        assertTrue(result.isSuccess)
        assertFalse(result.isFail)
    }

    @Test
    fun `isSuccess is false and isFail is true if failed`() {
        val result = DataResult.Fail(Failure.Network())
        assertFalse(result.isSuccess)
        assertTrue(result.isFail)
    }

    @Test
    fun `dataOrThrow returns data if successful`() {
        val payload = "Success"
        val result: DataResult<String> = DataResult.Success(payload)
        assertEquals(payload, result.dataOrThrow)
    }

    @Test(expected = IllegalStateException::class)
    fun `dataOrThrow throws exception data if failed`() {
        val result: DataResult<String> = DataResult.Fail(Failure.Network())
        result.dataOrThrow
    }

    @Test
    fun `dataOrNull returns data if successful`() {
        val payload = "Success"
        val result: DataResult<String> = DataResult.Success(payload)
        assertEquals(payload, result.dataOrNull)
    }

    @Test
    fun `dataOrNull returns null if failed`() {
        val result: DataResult<String> = DataResult.Fail(Failure.Network())
        assertNull(result.dataOrNull)
    }

    @Test
    fun `'map' function maps successful payload`() {
        val result = DataResult.Success(12345).map { it.toString() }
        assertEquals("12345", result.dataOrNull)
    }

    @Test
    fun `'map' function propagates failure`() {
        val expectedFailure = Failure.Network("Network failed!")
        var result: DataResult<String> = DataResult.Fail(expectedFailure)
        result = result.map { it.reversed() }
        var actualFailure: Failure? = null
        result.onFail<Failure.Network> { actualFailure = it }
        assertEquals(expectedFailure, actualFailure)
    }

    @Test
    fun `'then' function maps successful payload`() {
        val result = DataResult.Success(12345).then { DataResult.Success(it.toString()) }
        assertEquals("12345", result.dataOrNull)
    }

    @Test
    fun `'then' function propagates initial failure`() {
        val expectedFailure = Failure.Network("Network failed!")
        var result: DataResult<String> = DataResult.Fail(expectedFailure)
        result = result.then { DataResult.Success("") }
        var actualFailure: Failure? = null
        result.onFail<Failure.Network> { actualFailure = it }
        assertEquals(expectedFailure, actualFailure)
    }

    @Test
    fun `'then' function propagates failure after success`() {
        val expectedFailure = Failure.Network("Network failed!")
        val result: DataResult<String> = DataResult.Success(12345).then { DataResult.Fail(expectedFailure) }
        var actualFailure: Failure? = null
        result.onFail<Failure.Network> { actualFailure = it }
        assertEquals(expectedFailure, actualFailure)
    }

}
