/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.dashboard.widget.welcome

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeOfDayCalculatorTest {

    private val timeProvider: TimeProvider = mockk()
    private val calculator = TimeOfDayCalculator(timeProvider)

    @Test
    fun `getTimeOfDay returns NIGHT when hour is 0`() {
        every { timeProvider.getCurrentHourOfDay() } returns 0
        assertEquals(TimeOfDay.NIGHT, calculator.getTimeOfDay())
    }

    @Test
    fun `getTimeOfDay returns NIGHT when hour is 3`() {
        every { timeProvider.getCurrentHourOfDay() } returns 3
        assertEquals(TimeOfDay.NIGHT, calculator.getTimeOfDay())
    }

    @Test
    fun `getTimeOfDay returns MORNING when hour is 4`() {
        every { timeProvider.getCurrentHourOfDay() } returns 4
        assertEquals(TimeOfDay.MORNING, calculator.getTimeOfDay())
    }

    @Test
    fun `getTimeOfDay returns MORNING when hour is 8`() {
        every { timeProvider.getCurrentHourOfDay() } returns 8
        assertEquals(TimeOfDay.MORNING, calculator.getTimeOfDay())
    }

    @Test
    fun `getTimeOfDay returns MORNING when hour is 11`() {
        every { timeProvider.getCurrentHourOfDay() } returns 11
        assertEquals(TimeOfDay.MORNING, calculator.getTimeOfDay())
    }

    @Test
    fun `getTimeOfDay returns AFTERNOON when hour is 12`() {
        every { timeProvider.getCurrentHourOfDay() } returns 12
        assertEquals(TimeOfDay.AFTERNOON, calculator.getTimeOfDay())
    }

    @Test
    fun `getTimeOfDay returns AFTERNOON when hour is 14`() {
        every { timeProvider.getCurrentHourOfDay() } returns 14
        assertEquals(TimeOfDay.AFTERNOON, calculator.getTimeOfDay())
    }

    @Test
    fun `getTimeOfDay returns AFTERNOON when hour is 16`() {
        every { timeProvider.getCurrentHourOfDay() } returns 16
        assertEquals(TimeOfDay.AFTERNOON, calculator.getTimeOfDay())
    }

    @Test
    fun `getTimeOfDay returns EVENING when hour is 17`() {
        every { timeProvider.getCurrentHourOfDay() } returns 17
        assertEquals(TimeOfDay.EVENING, calculator.getTimeOfDay())
    }

    @Test
    fun `getTimeOfDay returns EVENING when hour is 19`() {
        every { timeProvider.getCurrentHourOfDay() } returns 19
        assertEquals(TimeOfDay.EVENING, calculator.getTimeOfDay())
    }

    @Test
    fun `getTimeOfDay returns EVENING when hour is 20`() {
        every { timeProvider.getCurrentHourOfDay() } returns 20
        assertEquals(TimeOfDay.EVENING, calculator.getTimeOfDay())
    }

    @Test
    fun `getTimeOfDay returns NIGHT when hour is 21`() {
        every { timeProvider.getCurrentHourOfDay() } returns 21
        assertEquals(TimeOfDay.NIGHT, calculator.getTimeOfDay())
    }

    @Test
    fun `getTimeOfDay returns NIGHT when hour is 22`() {
        every { timeProvider.getCurrentHourOfDay() } returns 22
        assertEquals(TimeOfDay.NIGHT, calculator.getTimeOfDay())
    }

    @Test
    fun `getTimeOfDay returns NIGHT when hour is 23`() {
        every { timeProvider.getCurrentHourOfDay() } returns 23
        assertEquals(TimeOfDay.NIGHT, calculator.getTimeOfDay())
    }
}
