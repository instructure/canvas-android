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
package com.instructure.student.features.appointmentgroups

import com.instructure.canvasapi2.apis.AppointmentGroupAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.models.AppointmentGroup
import com.instructure.canvasapi2.models.AppointmentSlot
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppointmentGroupRepositoryTest {

    private val appointmentGroupApi: AppointmentGroupAPI = mockk(relaxed = true)
    private val plannerApi: PlannerAPI.PlannerInterface = mockk(relaxed = true)

    private lateinit var repository: AppointmentGroupRepository

    @Before
    fun setUp() {
        repository = AppointmentGroupRepository(appointmentGroupApi, plannerApi)
    }

    @Test
    fun `getAppointmentGroups success returns Success with data`() = runTest {
        val courseId = 123L
        val expectedGroups = listOf(
            AppointmentGroup(
                id = 1L,
                title = "Office Hours",
                description = null,
                locationName = null,
                locationAddress = null,
                participantCount = 0,
                maxAppointmentsPerParticipant = 2,
                appointments = emptyList()
            )
        )

        coEvery {
            appointmentGroupApi.getAppointmentGroups(
                contextCodes = listOf("course_$courseId"),
                restParams = any()
            )
        } returns expectedGroups

        val result = repository.getAppointmentGroups(courseId, forceNetwork = false)

        assertTrue(result is DataResult.Success)
        assertEquals(expectedGroups, (result as DataResult.Success).data)
        coVerify {
            appointmentGroupApi.getAppointmentGroups(
                contextCodes = listOf("course_$courseId"),
                restParams = match { !it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getAppointmentGroups with forceNetwork passes correct RestParams`() = runTest {
        val courseId = 123L
        val expectedGroups = listOf(
            AppointmentGroup(
                id = 1L,
                title = "Office Hours",
                description = null,
                locationName = null,
                locationAddress = null,
                participantCount = 0,
                maxAppointmentsPerParticipant = null,
                appointments = emptyList()
            )
        )

        coEvery {
            appointmentGroupApi.getAppointmentGroups(
                contextCodes = any(),
                restParams = any()
            )
        } returns expectedGroups

        repository.getAppointmentGroups(courseId, forceNetwork = true)

        coVerify {
            appointmentGroupApi.getAppointmentGroups(
                contextCodes = listOf("course_$courseId"),
                restParams = match { it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getAppointmentGroups failure returns Fail`() = runTest {
        val courseId = 123L

        coEvery {
            appointmentGroupApi.getAppointmentGroups(
                contextCodes = any(),
                restParams = any()
            )
        } throws Exception("Network error")

        val result = repository.getAppointmentGroups(courseId, forceNetwork = false)

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `getUserEvents success returns Success with data`() = runTest {
        val startDate = "2025-11-01T00:00:00Z"
        val endDate = "2025-11-30T23:59:59Z"
        val expectedEvents = listOf<PlannerItem>(mockk())

        coEvery {
            plannerApi.getPlannerItems(
                startDate = startDate,
                endDate = endDate,
                contextCodes = emptyList(),
                filter = null,
                restParams = any()
            )
        } returns DataResult.Success(expectedEvents)

        val result = repository.getUserEvents(startDate, endDate, forceNetwork = false)

        assertTrue(result is DataResult.Success)
        assertEquals(expectedEvents, (result as DataResult.Success).data)
    }

    @Test
    fun `getUserEvents with forceNetwork passes correct RestParams`() = runTest {
        val startDate = "2025-11-01T00:00:00Z"
        val endDate = "2025-11-30T23:59:59Z"
        val expectedEvents = listOf<PlannerItem>(mockk())

        coEvery {
            plannerApi.getPlannerItems(
                startDate = any(),
                endDate = any(),
                contextCodes = any(),
                filter = any(),
                restParams = any()
            )
        } returns DataResult.Success(expectedEvents)

        repository.getUserEvents(startDate, endDate, forceNetwork = true)

        coVerify {
            plannerApi.getPlannerItems(
                startDate = startDate,
                endDate = endDate,
                contextCodes = emptyList(),
                filter = null,
                restParams = match { it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `getUserEvents failure returns Fail`() = runTest {
        val startDate = "2025-11-01T00:00:00Z"
        val endDate = "2025-11-30T23:59:59Z"

        coEvery {
            plannerApi.getPlannerItems(
                startDate = any(),
                endDate = any(),
                contextCodes = any(),
                filter = any(),
                restParams = any()
            )
        } returns DataResult.Fail()

        val result = repository.getUserEvents(startDate, endDate, forceNetwork = false)

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `reserveSlot success returns Success with reservation`() = runTest {
        val appointmentId = 100L
        val comments = "Looking forward to it"
        val expectedReservation = mockk<com.instructure.canvasapi2.models.ScheduleItem>()

        coEvery {
            appointmentGroupApi.reserveAppointmentSlot(
                appointmentId = appointmentId,
                body = any()
            )
        } returns expectedReservation

        val result = repository.reserveSlot(appointmentId, comments)

        assertTrue(result is DataResult.Success)
        assertEquals(expectedReservation, (result as DataResult.Success).data)
        coVerify {
            appointmentGroupApi.reserveAppointmentSlot(
                appointmentId = appointmentId,
                body = match { it.comments == comments }
            )
        }
    }

    @Test
    fun `reserveSlot with null comments passes null`() = runTest {
        val appointmentId = 100L
        val expectedReservation = mockk<com.instructure.canvasapi2.models.ScheduleItem>()

        coEvery {
            appointmentGroupApi.reserveAppointmentSlot(
                appointmentId = any(),
                body = any()
            )
        } returns expectedReservation

        repository.reserveSlot(appointmentId, null)

        coVerify {
            appointmentGroupApi.reserveAppointmentSlot(
                appointmentId = appointmentId,
                body = match { it.comments == null }
            )
        }
    }

    @Test
    fun `reserveSlot failure returns Fail`() = runTest {
        val appointmentId = 100L

        coEvery {
            appointmentGroupApi.reserveAppointmentSlot(
                appointmentId = any(),
                body = any()
            )
        } throws Exception("Network error")

        val result = repository.reserveSlot(appointmentId, null)

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `cancelReservation success returns Success`() = runTest {
        val reservationId = 999L

        coEvery {
            appointmentGroupApi.cancelAppointReservation(
                reservationId = reservationId,
                cancelReason = null
            )
        } returns Unit

        val result = repository.cancelReservation(reservationId)

        assertTrue(result is DataResult.Success)
        coVerify {
            appointmentGroupApi.cancelAppointReservation(
                reservationId = reservationId,
                cancelReason = null
            )
        }
    }

    @Test
    fun `cancelReservation failure returns Fail`() = runTest {
        val reservationId = 999L

        coEvery {
            appointmentGroupApi.cancelAppointReservation(
                reservationId = any(),
                cancelReason = any()
            )
        } throws Exception("Network error")

        val result = repository.cancelReservation(reservationId)

        assertTrue(result is DataResult.Fail)
    }
}
