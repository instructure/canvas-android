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

import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.appointmentgroups.AppointmentGroupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CancelAppointmentReservationUseCaseTest {

    private val repository: AppointmentGroupRepository = mockk(relaxed = true)

    private lateinit var useCase: CancelAppointmentReservationUseCase

    @Before
    fun setUp() {
        useCase = CancelAppointmentReservationUseCase(repository)
    }

    @Test
    fun `execute success returns Success`() = runTest {
        val reservationId = 999L

        coEvery {
            repository.cancelReservation(reservationId)
        } returns DataResult.Success(Unit)

        val result = useCase(CancelAppointmentReservationUseCase.Params(reservationId))

        assertTrue(result is DataResult.Success)
        coVerify { repository.cancelReservation(reservationId) }
    }

    @Test
    fun `execute failure returns Fail`() = runTest {
        val reservationId = 999L

        coEvery {
            repository.cancelReservation(any())
        } returns DataResult.Fail()

        val result = useCase(CancelAppointmentReservationUseCase.Params(reservationId))

        assertTrue(result is DataResult.Fail)
    }
}
