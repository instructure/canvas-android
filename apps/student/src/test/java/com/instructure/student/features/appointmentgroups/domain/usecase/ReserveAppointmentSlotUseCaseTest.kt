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
 */
package com.instructure.student.features.appointmentgroups.domain.usecase

import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.appointmentgroups.AppointmentGroupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReserveAppointmentSlotUseCaseTest {

    private val repository: AppointmentGroupRepository = mockk(relaxed = true)

    private lateinit var useCase: ReserveAppointmentSlotUseCase

    @Before
    fun setUp() {
        useCase = ReserveAppointmentSlotUseCase(repository)
    }

    @Test
    fun `execute success returns Success with reservation`() = runTest {
        val appointmentId = 100L
        val comments = "Looking forward to it"
        val expectedReservation = mockk<ScheduleItem> {
            io.mockk.every { id } returns 999L
        }

        coEvery {
            repository.reserveSlot(appointmentId, comments)
        } returns DataResult.Success(expectedReservation)

        val result = useCase(ReserveAppointmentSlotUseCase.Params(appointmentId, comments))

        assertTrue(result is DataResult.Success)
        assertEquals(expectedReservation, (result as DataResult.Success).data)
        coVerify { repository.reserveSlot(appointmentId, comments) }
    }

    @Test
    fun `execute with null comments passes null to repository`() = runTest {
        val appointmentId = 100L
        val expectedReservation = mockk<ScheduleItem>()

        coEvery {
            repository.reserveSlot(appointmentId, null)
        } returns DataResult.Success(expectedReservation)

        val result = useCase(ReserveAppointmentSlotUseCase.Params(appointmentId, null))

        assertTrue(result is DataResult.Success)
        coVerify { repository.reserveSlot(appointmentId, null) }
    }

    @Test
    fun `execute with empty comments passes empty string to repository`() = runTest {
        val appointmentId = 100L
        val comments = ""
        val expectedReservation = mockk<ScheduleItem>()

        coEvery {
            repository.reserveSlot(appointmentId, comments)
        } returns DataResult.Success(expectedReservation)

        val result = useCase(ReserveAppointmentSlotUseCase.Params(appointmentId, comments))

        assertTrue(result is DataResult.Success)
        coVerify { repository.reserveSlot(appointmentId, comments) }
    }

    @Test
    fun `execute failure returns Fail`() = runTest {
        val appointmentId = 100L
        val comments = "Test"

        coEvery {
            repository.reserveSlot(any(), any())
        } returns DataResult.Fail()

        val result = useCase(ReserveAppointmentSlotUseCase.Params(appointmentId, comments))

        assertTrue(result is DataResult.Fail)
    }
}
