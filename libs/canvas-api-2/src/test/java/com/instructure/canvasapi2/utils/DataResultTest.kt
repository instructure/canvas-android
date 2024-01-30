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
package com.instructure.canvasapi2.utils

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class DataResultTest : Assert() {

    @Test
    fun `isSuccess is true when successful`() {
        val result: DataResult<String> = DataResult.Success("test")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `isSuccess is false when failed`() {
        val result: DataResult<String> = DataResult.Fail()
        assertFalse(result.isSuccess)
    }

    @Test
    fun `isFail is false when successful`() {
        val result: DataResult<String> = DataResult.Success("test")
        assertFalse(result.isFail)
    }

    @Test
    fun `isFail is true when failed`() {
        val result: DataResult<String> = DataResult.Fail()
        assertTrue(result.isFail)
    }

    @Test
    fun `dataOrNull returns success value when successful`() {
        val expectedValue = "test"
        val result: DataResult<String> = DataResult.Success(expectedValue)
        val actualValue: String? = result.dataOrNull
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `dataOrNull returns null when failed`() {
        val result: DataResult<String> = DataResult.Fail()
        val actualValue: String? = result.dataOrNull
        assertNull(actualValue)
    }

    @Test
    fun `Maps successful value to new value`() {
        val successValue = "test"
        val expectedValue = successValue.length
        val result: DataResult<String> = DataResult.Success(successValue)
        val mappedResult: DataResult<Int> = result.map { it.length }
        val actualValue = mappedResult.dataOrNull
        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `Maps failed value to failed value`() {
        val result: DataResult<String> = DataResult.Fail()
        val mappedResult: DataResult<Int> = result.map { it.length }
        assertEquals(result, mappedResult)
    }

    @Test
    fun `Executes onSuccess block with value when successful`() {
        val expectedValue = "test"
        val result: DataResult<String> = DataResult.Success(expectedValue)
        val block = mockk<(String) -> Unit>(relaxed = true)
        result.onSuccess(block)
        verify(exactly = 1) { block.invoke(expectedValue) }
    }

    @Test
    fun `Does not execute onSuccess block when failed`() {
        val result: DataResult<String> = DataResult.Fail()
        val block = mockk<(String) -> Unit>(relaxed = true)
        result.onSuccess(block)
        verify(exactly = 0) { block.invoke(capture(slot())) }
    }

    @Test
    fun `Executes onFail with Exception type block with values when failed`() {
        val expectedFailure = Failure.Exception(RuntimeException("test"), "test")
        val result: DataResult<String> = DataResult.Fail(expectedFailure)
        val block = mockk<(Failure) -> Unit>(relaxed = true)
        result.onFail<Failure.Exception>(block)
        verify(exactly = 1) { block.invoke(expectedFailure) }
    }

    @Test
    fun `Executes onFail with Network type block with values when failed`() {
        val expectedFailure = Failure.Network("test")
        val result: DataResult<String> = DataResult.Fail(expectedFailure)
        val block = mockk<(Failure) -> Unit>(relaxed = true)
        result.onFail<Failure.Network>(block)
        verify(exactly = 1) { block.invoke(expectedFailure) }
    }

    @Test
    fun `Executes onFail with Authorization type block with values when failed`() {
        val expectedFailure = Failure.Authorization("test")
        val result: DataResult<String> = DataResult.Fail(expectedFailure)
        val block = mockk<(Failure) -> Unit>(relaxed = true)
        result.onFail<Failure.Authorization>(block)
        verify(exactly = 1) { block.invoke(expectedFailure) }
    }

    @Test
    fun `Executes onFailure block when failed`() {
        val result: DataResult<String> = DataResult.Fail()
        val block = mockk<(Failure?) -> Unit>(relaxed = true)
        result.onFailure(block)
        verify(exactly = 1) { block.invoke(null) }
    }

    @Test
    fun `Executes onFailure block with values when failed`() {
        val expectedFailure = Failure.Network()
        val result: DataResult<String> = DataResult.Fail(expectedFailure)
        val block = mockk<(Failure?) -> Unit>(relaxed = true)
        result.onFailure(block)
        verify(exactly = 1) { block.invoke(expectedFailure) }
    }

    @Test
    fun `Does not execute onFailure block when successful`() {
        val result: DataResult<String> = DataResult.Success("Test")
        val block = mockk<(Failure?) -> Unit>(relaxed = true)
        result.onFailure(block)
        verify(exactly = 0) { block.invoke(capture(slot())) }
    }

    @Test
    fun `Does not execute onFail block when successful`() {
        val result: DataResult<String> = DataResult.Success("Test")
        val block = mockk<(Failure) -> Unit>(relaxed = true)
        result.onFail<Failure.Network>(block)
        verify(exactly = 0) { block.invoke(capture(slot())) }
    }
}
