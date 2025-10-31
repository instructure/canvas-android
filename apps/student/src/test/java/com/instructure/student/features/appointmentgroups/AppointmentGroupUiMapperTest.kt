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

import com.instructure.student.features.appointmentgroups.domain.model.AppointmentGroupDomain
import com.instructure.student.features.appointmentgroups.domain.model.AppointmentSlotDomain
import com.instructure.student.features.appointmentgroups.domain.model.ConflictInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppointmentGroupUiMapperTest {

    private lateinit var mapper: AppointmentGroupUiMapper

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    @Before
    fun setUp() {
        mapper = AppointmentGroupUiMapper()
    }

    @Test
    fun `mapToUiState maps basic group properties correctly`() {
        val domainGroup = AppointmentGroupDomain(
            id = 1L,
            title = "Office Hours",
            description = "Weekly office hours",
            locationName = "Room 301",
            locationAddress = "123 Main St",
            participantCount = 5,
            maxAppointmentsPerParticipant = null,
            slots = emptyList()
        )

        val result = mapper.mapToUiState(listOf(domainGroup))

        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("Office Hours", result[0].title)
        assertEquals("Weekly office hours", result[0].description)
        assertEquals("Room 301", result[0].locationName)
        assertEquals("123 Main St", result[0].locationAddress)
        assertEquals(5, result[0].participantCount)
        assertNull(result[0].maxAppointmentsPerParticipant)
        assertEquals(0, result[0].currentReservationCount)
        assertTrue(result[0].canReserveMore)
        assertTrue(result[0].isExpanded)
    }

    @Test
    fun `mapToUiState handles null description and location`() {
        val domainGroup = AppointmentGroupDomain(
            id = 1L,
            title = "Office Hours",
            description = null,
            locationName = null,
            locationAddress = null,
            participantCount = 0,
            maxAppointmentsPerParticipant = null,
            slots = emptyList()
        )

        val result = mapper.mapToUiState(listOf(domainGroup))

        assertNull(result[0].description)
        assertNull(result[0].locationName)
        assertNull(result[0].locationAddress)
    }

    @Test
    fun `mapToUiState maps slots with available slots correctly`() {
        val startDate = dateFormat.parse("2025-11-06 16:00")!!
        val endDate = dateFormat.parse("2025-11-06 16:30")!!

        val slot = AppointmentSlotDomain(
            id = 100L,
            appointmentGroupId = 1L,
            startDate = startDate,
            endDate = endDate,
            availableSlots = 5,
            isReservedByMe = false,
            myReservationId = null,
            conflictInfo = ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        )

        val domainGroup = AppointmentGroupDomain(
            id = 1L,
            title = "Office Hours",
            description = null,
            locationName = null,
            locationAddress = null,
            participantCount = 0,
            maxAppointmentsPerParticipant = null,
            slots = listOf(slot)
        )

        val result = mapper.mapToUiState(listOf(domainGroup))

        assertEquals(1, result[0].slots.size)
        assertEquals(100L, result[0].slots[0].id)
        assertEquals(5, result[0].slots[0].availableSlots)
        assertTrue(result[0].slots[0].isAvailable)
        assertFalse(result[0].slots[0].isReservedByMe)
        assertNull(result[0].slots[0].myReservationId)
        assertFalse(result[0].slots[0].hasConflict)
    }

    @Test
    fun `mapToUiState maps reserved slot correctly`() {
        val startDate = dateFormat.parse("2025-11-06 16:00")!!
        val endDate = dateFormat.parse("2025-11-06 16:30")!!

        val slot = AppointmentSlotDomain(
            id = 100L,
            appointmentGroupId = 1L,
            startDate = startDate,
            endDate = endDate,
            availableSlots = 4,
            isReservedByMe = true,
            myReservationId = 999L,
            conflictInfo = ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        )

        val domainGroup = AppointmentGroupDomain(
            id = 1L,
            title = "Office Hours",
            description = null,
            locationName = null,
            locationAddress = null,
            participantCount = 0,
            maxAppointmentsPerParticipant = null,
            slots = listOf(slot)
        )

        val result = mapper.mapToUiState(listOf(domainGroup))

        assertTrue(result[0].slots[0].isReservedByMe)
        assertEquals(999L, result[0].slots[0].myReservationId)
        assertFalse(result[0].slots[0].isAvailable)
    }

    @Test
    fun `mapToUiState maps slot with no available slots`() {
        val startDate = dateFormat.parse("2025-11-06 16:00")!!
        val endDate = dateFormat.parse("2025-11-06 16:30")!!

        val slot = AppointmentSlotDomain(
            id = 100L,
            appointmentGroupId = 1L,
            startDate = startDate,
            endDate = endDate,
            availableSlots = 0,
            isReservedByMe = false,
            myReservationId = null,
            conflictInfo = ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        )

        val domainGroup = AppointmentGroupDomain(
            id = 1L,
            title = "Office Hours",
            description = null,
            locationName = null,
            locationAddress = null,
            participantCount = 0,
            maxAppointmentsPerParticipant = null,
            slots = listOf(slot)
        )

        val result = mapper.mapToUiState(listOf(domainGroup))

        assertEquals(0, result[0].slots[0].availableSlots)
        assertFalse(result[0].slots[0].isAvailable)
    }

    @Test
    fun `mapToUiState maps slot with conflict correctly`() {
        val startDate = dateFormat.parse("2025-11-06 16:00")!!
        val endDate = dateFormat.parse("2025-11-06 16:30")!!

        val slot = AppointmentSlotDomain(
            id = 100L,
            appointmentGroupId = 1L,
            startDate = startDate,
            endDate = endDate,
            availableSlots = 5,
            isReservedByMe = false,
            myReservationId = null,
            conflictInfo = ConflictInfo(hasConflict = true, conflictingEventTitle = "Math Assignment")
        )

        val domainGroup = AppointmentGroupDomain(
            id = 1L,
            title = "Office Hours",
            description = null,
            locationName = null,
            locationAddress = null,
            participantCount = 0,
            maxAppointmentsPerParticipant = null,
            slots = listOf(slot)
        )

        val result = mapper.mapToUiState(listOf(domainGroup))

        assertTrue(result[0].slots[0].hasConflict)
        assertEquals("Math Assignment", result[0].slots[0].conflictEventTitle)
    }

    @Test
    fun `mapToUiState maps multiple groups`() {
        val group1 = AppointmentGroupDomain(
            id = 1L,
            title = "Office Hours",
            description = null,
            locationName = null,
            locationAddress = null,
            participantCount = 0,
            maxAppointmentsPerParticipant = null,
            slots = emptyList()
        )

        val group2 = AppointmentGroupDomain(
            id = 2L,
            title = "Lab Sessions",
            description = null,
            locationName = null,
            locationAddress = null,
            participantCount = 0,
            maxAppointmentsPerParticipant = null,
            slots = emptyList()
        )

        val result = mapper.mapToUiState(listOf(group1, group2))

        assertEquals(2, result.size)
        assertEquals("Office Hours", result[0].title)
        assertEquals("Lab Sessions", result[1].title)
    }

    @Test
    fun `mapToUiState formats time range correctly`() {
        val startDate = dateFormat.parse("2025-11-06 14:00")!!
        val endDate = dateFormat.parse("2025-11-06 15:30")!!

        val slot = AppointmentSlotDomain(
            id = 100L,
            appointmentGroupId = 1L,
            startDate = startDate,
            endDate = endDate,
            availableSlots = 5,
            isReservedByMe = false,
            myReservationId = null,
            conflictInfo = ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        )

        val domainGroup = AppointmentGroupDomain(
            id = 1L,
            title = "Office Hours",
            description = null,
            locationName = null,
            locationAddress = null,
            participantCount = 0,
            maxAppointmentsPerParticipant = null,
            slots = listOf(slot)
        )

        val result = mapper.mapToUiState(listOf(domainGroup))

        val timeRange = result[0].slots[0].timeRange
        assertTrue(timeRange.contains("2:00 PM") || timeRange.contains("14:00"))
        assertTrue(timeRange.contains("3:30 PM") || timeRange.contains("15:30"))
    }

    @Test
    fun `mapToUiState handles empty list`() {
        val result = mapper.mapToUiState(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `isAvailable is false when reserved by me even with available slots`() {
        val startDate = dateFormat.parse("2025-11-06 16:00")!!
        val endDate = dateFormat.parse("2025-11-06 16:30")!!

        val slot = AppointmentSlotDomain(
            id = 100L,
            appointmentGroupId = 1L,
            startDate = startDate,
            endDate = endDate,
            availableSlots = 5,
            isReservedByMe = true,
            myReservationId = 999L,
            conflictInfo = ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        )

        val domainGroup = AppointmentGroupDomain(
            id = 1L,
            title = "Office Hours",
            description = null,
            locationName = null,
            locationAddress = null,
            participantCount = 0,
            maxAppointmentsPerParticipant = null,
            slots = listOf(slot)
        )

        val result = mapper.mapToUiState(listOf(domainGroup))

        assertFalse(result[0].slots[0].isAvailable)
        assertTrue(result[0].slots[0].isReservedByMe)
    }

    @Test
    fun `mapToUiState calculates canReserveMore correctly with no limit`() {
        val slot1 = AppointmentSlotDomain(
            id = 100L,
            appointmentGroupId = 1L,
            startDate = dateFormat.parse("2025-11-06 16:00")!!,
            endDate = dateFormat.parse("2025-11-06 16:30")!!,
            availableSlots = 5,
            isReservedByMe = true,
            myReservationId = 999L,
            conflictInfo = ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        )

        val domainGroup = AppointmentGroupDomain(
            id = 1L,
            title = "Office Hours",
            description = null,
            locationName = null,
            locationAddress = null,
            participantCount = 0,
            maxAppointmentsPerParticipant = null,
            slots = listOf(slot1)
        )

        val result = mapper.mapToUiState(listOf(domainGroup))

        assertNull(result[0].maxAppointmentsPerParticipant)
        assertEquals(1, result[0].currentReservationCount)
        assertTrue(result[0].canReserveMore)
    }

    @Test
    fun `mapToUiState calculates canReserveMore correctly when under limit`() {
        val slot1 = AppointmentSlotDomain(
            id = 100L,
            appointmentGroupId = 1L,
            startDate = dateFormat.parse("2025-11-06 16:00")!!,
            endDate = dateFormat.parse("2025-11-06 16:30")!!,
            availableSlots = 5,
            isReservedByMe = true,
            myReservationId = 999L,
            conflictInfo = ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        )

        val domainGroup = AppointmentGroupDomain(
            id = 1L,
            title = "Office Hours",
            description = null,
            locationName = null,
            locationAddress = null,
            participantCount = 0,
            maxAppointmentsPerParticipant = 2,
            slots = listOf(slot1)
        )

        val result = mapper.mapToUiState(listOf(domainGroup))

        assertEquals(2, result[0].maxAppointmentsPerParticipant)
        assertEquals(1, result[0].currentReservationCount)
        assertTrue(result[0].canReserveMore)
    }

    @Test
    fun `mapToUiState calculates canReserveMore correctly when at limit`() {
        val slot1 = AppointmentSlotDomain(
            id = 100L,
            appointmentGroupId = 1L,
            startDate = dateFormat.parse("2025-11-06 16:00")!!,
            endDate = dateFormat.parse("2025-11-06 16:30")!!,
            availableSlots = 5,
            isReservedByMe = true,
            myReservationId = 999L,
            conflictInfo = ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        )

        val slot2 = AppointmentSlotDomain(
            id = 101L,
            appointmentGroupId = 1L,
            startDate = dateFormat.parse("2025-11-06 17:00")!!,
            endDate = dateFormat.parse("2025-11-06 17:30")!!,
            availableSlots = 5,
            isReservedByMe = true,
            myReservationId = 1000L,
            conflictInfo = ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        )

        val domainGroup = AppointmentGroupDomain(
            id = 1L,
            title = "Office Hours",
            description = null,
            locationName = null,
            locationAddress = null,
            participantCount = 0,
            maxAppointmentsPerParticipant = 2,
            slots = listOf(slot1, slot2)
        )

        val result = mapper.mapToUiState(listOf(domainGroup))

        assertEquals(2, result[0].maxAppointmentsPerParticipant)
        assertEquals(2, result[0].currentReservationCount)
        assertFalse(result[0].canReserveMore)
    }

    @Test
    fun `mapToUiState calculates canReserveMore correctly when over limit`() {
        val slot1 = AppointmentSlotDomain(
            id = 100L,
            appointmentGroupId = 1L,
            startDate = dateFormat.parse("2025-11-06 16:00")!!,
            endDate = dateFormat.parse("2025-11-06 16:30")!!,
            availableSlots = 5,
            isReservedByMe = true,
            myReservationId = 999L,
            conflictInfo = ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        )

        val slot2 = AppointmentSlotDomain(
            id = 101L,
            appointmentGroupId = 1L,
            startDate = dateFormat.parse("2025-11-06 17:00")!!,
            endDate = dateFormat.parse("2025-11-06 17:30")!!,
            availableSlots = 5,
            isReservedByMe = true,
            myReservationId = 1000L,
            conflictInfo = ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        )

        val slot3 = AppointmentSlotDomain(
            id = 102L,
            appointmentGroupId = 1L,
            startDate = dateFormat.parse("2025-11-06 18:00")!!,
            endDate = dateFormat.parse("2025-11-06 18:30")!!,
            availableSlots = 5,
            isReservedByMe = true,
            myReservationId = 1001L,
            conflictInfo = ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        )

        val domainGroup = AppointmentGroupDomain(
            id = 1L,
            title = "Office Hours",
            description = null,
            locationName = null,
            locationAddress = null,
            participantCount = 0,
            maxAppointmentsPerParticipant = 2,
            slots = listOf(slot1, slot2, slot3)
        )

        val result = mapper.mapToUiState(listOf(domainGroup))

        assertEquals(2, result[0].maxAppointmentsPerParticipant)
        assertEquals(3, result[0].currentReservationCount)
        assertFalse(result[0].canReserveMore)
    }
}
