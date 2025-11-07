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

import com.instructure.canvasapi2.models.AppointmentGroup
import com.instructure.canvasapi2.models.AppointmentSlot
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.features.appointmentgroups.AppointmentGroupRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAppointmentGroupsUseCaseTest {

    private val repository: AppointmentGroupRepository = mockk(relaxed = true)

    private lateinit var useCase: GetAppointmentGroupsUseCase

    @Before
    fun setUp() {
        useCase = GetAppointmentGroupsUseCase(repository, mockk(relaxed = true))
    }

    @Test
    fun `execute returns Fail when getAppointmentGroups fails`() = runTest {
        val courseIds = listOf(123L)

        coEvery { repository.getAppointmentGroups(any(), any(), any(), any()) } returns DataResult.Fail()

        val result = useCase(GetAppointmentGroupsUseCase.Params(courseIds))

        assertTrue(result is DataResult.Fail)
    }

    @Test
    fun `execute returns Success with domain groups when repository succeeds`() = runTest {
        val courseIds = listOf(123L)
        val startDate = "2025-11-06T16:00:00Z"
        val endDate = "2025-11-06T16:30:00Z"

        val appointmentGroups = listOf(
            AppointmentGroup(
                id = 1L,
                title = "Office Hours",
                description = null,
                locationName = null,
                locationAddress = null,
                participantCount = 0,
                maxAppointmentsPerParticipant = 2,
                appointments = listOf(
                    AppointmentSlot(
                        id = 100L,
                        appointmentGroupId = 1L,
                        startAt = startDate,
                        endAt = endDate,
                        childEvents = emptyList(),
                        availableSlots = 5
                    )
                )
            )
        )

        coEvery { repository.getAppointmentGroups(any(), any(), any(), any()) } returns DataResult.Success(appointmentGroups)
        coEvery { repository.getUserEvents(any(), any(), false) } returns DataResult.Success(emptyList())

        val result = useCase(GetAppointmentGroupsUseCase.Params(courseIds))

        assertTrue(result is DataResult.Success)
        val groups = (result as DataResult.Success).data
        assertEquals(1, groups.size)
        assertEquals("Office Hours", groups[0].title)
        assertEquals(1, groups[0].slots.size)
        assertEquals(100L, groups[0].slots[0].id)
        assertEquals(2, groups[0].maxAppointmentsPerParticipant)
    }

    @Test
    fun `execute passes forceNetwork parameter to repository`() = runTest {
        val courseIds = listOf(123L)
        val appointmentGroups = listOf(
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

        coEvery { repository.getAppointmentGroups(any(), any(), any(), any()) } returns DataResult.Success(appointmentGroups)
        coEvery { repository.getUserEvents(any(), any(), true) } returns DataResult.Success(emptyList())

        useCase(GetAppointmentGroupsUseCase.Params(courseIds, forceNetwork = true))

        io.mockk.coVerify { repository.getAppointmentGroups(any(), any(), any(), any()) }
        io.mockk.coVerify { repository.getUserEvents(any(), any(), true) }
    }

    @Test
    fun `execute passes includePastAppointments parameter to repository`() = runTest {
        val courseIds = listOf(123L)
        val appointmentGroups = listOf(
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

        coEvery { repository.getAppointmentGroups(any(), any(), any(), any()) } returns DataResult.Success(appointmentGroups)
        coEvery { repository.getUserEvents(any(), any(), false) } returns DataResult.Success(emptyList())

        useCase(GetAppointmentGroupsUseCase.Params(courseIds, includePastAppointments = true))

        io.mockk.coVerify { repository.getAppointmentGroups(any(), any(), any(), any()) }
    }

    @Test
    fun `execute handles null startAt or endAt without crashing`() = runTest {
        val courseIds = listOf(123L)

        val appointmentGroups = listOf(
            AppointmentGroup(
                id = 1L,
                title = "Office Hours",
                description = null,
                locationName = null,
                locationAddress = null,
                participantCount = 0,
                maxAppointmentsPerParticipant = null,
                appointments = listOf(
                    AppointmentSlot(
                        id = 100L,
                        appointmentGroupId = 1L,
                        startAt = null,
                        endAt = null,
                        childEvents = emptyList(),
                        availableSlots = 5
                    )
                )
            )
        )

        coEvery { repository.getAppointmentGroups(any(), any(), any(), any()) } returns DataResult.Success(appointmentGroups)
        coEvery { repository.getUserEvents(any(), any(), false) } returns DataResult.Success(emptyList())

        val result = useCase(GetAppointmentGroupsUseCase.Params(courseIds))

        assertTrue(result is DataResult.Success)
        val groups = (result as DataResult.Success).data
        assertFalse(groups[0].slots[0].conflictInfo?.hasConflict == true)
    }

    @Test
    fun `execute continues when getUserEvents fails`() = runTest {
        val courseIds = listOf(123L)
        val startDate = "2025-11-06T16:00:00Z"
        val endDate = "2025-11-06T16:30:00Z"

        val appointmentGroups = listOf(
            AppointmentGroup(
                id = 1L,
                title = "Office Hours",
                description = null,
                locationName = null,
                locationAddress = null,
                participantCount = 0,
                maxAppointmentsPerParticipant = null,
                appointments = listOf(
                    AppointmentSlot(
                        id = 100L,
                        appointmentGroupId = 1L,
                        startAt = startDate,
                        endAt = endDate,
                        childEvents = emptyList(),
                        availableSlots = 5
                    )
                )
            )
        )

        coEvery { repository.getAppointmentGroups(any(), any(), any(), any()) } returns DataResult.Success(appointmentGroups)
        coEvery { repository.getUserEvents(any(), any(), false) } returns DataResult.Fail()

        val result = useCase(GetAppointmentGroupsUseCase.Params(courseIds))

        assertTrue(result is DataResult.Success)
        val groups = (result as DataResult.Success).data
        assertEquals(1, groups.size)
        assertFalse(groups[0].slots[0].conflictInfo?.hasConflict == true)
    }

    @Test
    fun `execute handles empty appointments list`() = runTest {
        val courseIds = listOf(123L)

        val appointmentGroups = listOf(
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

        coEvery { repository.getAppointmentGroups(any(), any(), any(), any()) } returns DataResult.Success(appointmentGroups)
        coEvery { repository.getUserEvents(any(), any(), false) } returns DataResult.Success(emptyList())

        val result = useCase(GetAppointmentGroupsUseCase.Params(courseIds))

        assertTrue(result is DataResult.Success)
        val groups = (result as DataResult.Success).data
        assertEquals(1, groups.size)
        assertTrue(groups[0].slots.isEmpty())
    }

    @Test
    fun `execute supports multiple courseIds`() = runTest {
        val courseIds = listOf(123L, 456L, 789L)
        val appointmentGroups = listOf(
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

        coEvery { repository.getAppointmentGroups(any(), any(), any(), any()) } returns DataResult.Success(appointmentGroups)
        coEvery { repository.getUserEvents(any(), any(), false) } returns DataResult.Success(emptyList())

        val result = useCase(GetAppointmentGroupsUseCase.Params(courseIds))

        assertTrue(result is DataResult.Success)
        io.mockk.coVerify { repository.getAppointmentGroups(any(), any(), any(), any()) }
    }
}
