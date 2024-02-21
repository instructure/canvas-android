/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.calendar

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate

class CalendarStateMapperTest {

    private val calendarStateMapper = CalendarStateMapper()

    @Test
    fun `Format header UI state for selected date`() {
        val headerUiState = calendarStateMapper.createHeaderUiState(LocalDate.of(2023, 4, 20), null)

        assertEquals("2023", headerUiState.yearTitle)
        assertEquals("April", headerUiState.monthTitle)
    }

    @Test
    fun `Return one row body for calendar when it's not expanded with weekends disabled`() {
        val bodyUiState = calendarStateMapper.createBodyUiState(false, LocalDate.of(2023, 4, 20))

        assertEquals(1, bodyUiState.currentPage.calendarRows.size)

        val expectedCalendarRow = CalendarRowUiState(
            listOf(
                CalendarDayUiState(16, LocalDate.of(2023, 4, 16), false),
                CalendarDayUiState(17, LocalDate.of(2023, 4, 17), true),
                CalendarDayUiState(18, LocalDate.of(2023, 4, 18), true),
                CalendarDayUiState(19, LocalDate.of(2023, 4, 19), true),
                CalendarDayUiState(20, LocalDate.of(2023, 4, 20), true),
                CalendarDayUiState(21, LocalDate.of(2023, 4, 21), true),
                CalendarDayUiState(22, LocalDate.of(2023, 4, 22), false),
            )
        )

        assertEquals(expectedCalendarRow, bodyUiState.currentPage.calendarRows.first())
    }

    @Test
    fun `Return all rows for the month for calendar when it's expanded with disabled days from previous month`() {
        val bodyUiState = calendarStateMapper.createBodyUiState(true, LocalDate.of(2023, 4, 20))

        assertEquals(6, bodyUiState.currentPage.calendarRows.size)

        val expectedFirstCalendarRow = CalendarRowUiState(
            listOf(
                CalendarDayUiState(26, LocalDate.of(2023, 3, 26), false),
                CalendarDayUiState(27, LocalDate.of(2023, 3, 27), false),
                CalendarDayUiState(28, LocalDate.of(2023, 3, 28), false),
                CalendarDayUiState(29, LocalDate.of(2023, 3, 29), false),
                CalendarDayUiState(30, LocalDate.of(2023, 3, 30), false),
                CalendarDayUiState(31, LocalDate.of(2023, 3, 31), false),
                CalendarDayUiState(1, LocalDate.of(2023, 4, 1), false),
            )
        )

        assertEquals(expectedFirstCalendarRow, bodyUiState.currentPage.calendarRows.first())
    }

    @Test
    fun `Add correct indicator count for days`() {
        val bodyUiState = calendarStateMapper.createBodyUiState(
            expanded = false, LocalDate.of(2023, 4, 20), eventIndicators = mapOf(
                LocalDate.of(2023, 4, 20) to 1,
                LocalDate.of(2023, 4, 22) to 2,
                LocalDate.of(2023, 4, 18) to 3,
            )
        )

        val expectedCalendarRow = CalendarRowUiState(
            listOf(
                CalendarDayUiState(16, LocalDate.of(2023, 4, 16), false),
                CalendarDayUiState(17, LocalDate.of(2023, 4, 17), true),
                CalendarDayUiState(18, LocalDate.of(2023, 4, 18), true, indicatorCount = 3),
                CalendarDayUiState(19, LocalDate.of(2023, 4, 19), true),
                CalendarDayUiState(20, LocalDate.of(2023, 4, 20), true, indicatorCount = 1),
                CalendarDayUiState(21, LocalDate.of(2023, 4, 21), true),
                CalendarDayUiState(22, LocalDate.of(2023, 4, 22), false, indicatorCount = 2),
            )
        )

        assertEquals(expectedCalendarRow, bodyUiState.currentPage.calendarRows.first())
    }
}