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

import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.features.appointmentgroups.AppointmentGroupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CancelReservationUseCaseTest {

    private val repository: AppointmentGroupRepository = mockk(relaxed = true)
    private lateinit var useCase: CancelReservationUseCase

    @Before
    fun setUp() {
        useCase = CancelReservationUseCase(repository)
    }

    @Test
    fun `execute returns Fail when repository fails`() = runTest {
        val reservationId = 999L

        coEvery { repository.cancelReservation(any()) } returns DataResult.Fail()

        val result = useCase(CancelReservationUseCase.Params(reservationId))

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `execute returns Success when repository succeeds`() = runTest {
        val reservationId = 999L

        coEvery { repository.cancelReservation(any()) } returns DataResult.Success(Unit)

        val result = useCase(CancelReservationUseCase.Params(reservationId))

        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `execute passes reservationId to repository`() = runTest {
        val reservationId = 999L

        coEvery { repository.cancelReservation(any()) } returns DataResult.Success(Unit)

        useCase(CancelReservationUseCase.Params(reservationId))

        coVerify { repository.cancelReservation(eq(reservationId)) }
    }
}
