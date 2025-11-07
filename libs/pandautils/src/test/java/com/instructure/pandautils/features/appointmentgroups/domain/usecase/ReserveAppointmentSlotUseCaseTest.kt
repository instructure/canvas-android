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
package com.instructure.pandautils.features.appointmentgroups.domain.usecase

import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.features.appointmentgroups.AppointmentGroupRepository
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
    fun `execute returns Fail when repository fails`() = runTest {
        val appointmentId = 100L

        coEvery { repository.reserveSlot(any(), any()) } returns DataResult.Fail()

        val result = useCase(ReserveAppointmentSlotUseCase.Params(appointmentId))

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `execute returns Success with ScheduleItem when repository succeeds`() = runTest {
        val appointmentId = 100L
        val scheduleItem = ScheduleItem(itemId = "999")

        coEvery { repository.reserveSlot(any(), any()) } returns DataResult.Success(scheduleItem)

        val result = useCase(ReserveAppointmentSlotUseCase.Params(appointmentId))

        assertTrue(result is DataResult.Success)
        assertEquals("999", (result as DataResult.Success).data.itemId)
    }

    @Test
    fun `execute passes appointmentId to repository`() = runTest {
        val appointmentId = 100L
        val scheduleItem = ScheduleItem(itemId = "999")

        coEvery { repository.reserveSlot(any(), any()) } returns DataResult.Success(scheduleItem)

        useCase(ReserveAppointmentSlotUseCase.Params(appointmentId))

        coVerify { repository.reserveSlot(eq(appointmentId), any()) }
    }

    @Test
    fun `execute passes comments to repository`() = runTest {
        val appointmentId = 100L
        val comments = "Test comment"
        val scheduleItem = ScheduleItem(itemId = "999")

        coEvery { repository.reserveSlot(any(), any()) } returns DataResult.Success(scheduleItem)

        useCase(ReserveAppointmentSlotUseCase.Params(appointmentId, comments))

        coVerify { repository.reserveSlot(any(), eq(comments)) }
    }

    @Test
    fun `execute handles null comments`() = runTest {
        val appointmentId = 100L
        val scheduleItem = ScheduleItem(itemId = "999")

        coEvery { repository.reserveSlot(any(), any()) } returns DataResult.Success(scheduleItem)

        val result = useCase(ReserveAppointmentSlotUseCase.Params(appointmentId, null))

        assertTrue(result is DataResult.Success)
        coVerify { repository.reserveSlot(appointmentId, null) }
    }
}
