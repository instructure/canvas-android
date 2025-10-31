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
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class AppointmentGroupUiMapper @Inject constructor() {

    fun mapToUiState(domainGroups: List<AppointmentGroupDomain>): List<AppointmentGroupUiState> {
        return domainGroups.map { group ->
            val currentReservationCount = group.slots.count { it.isReservedByMe }
            val maxLimit = group.maxAppointmentsPerParticipant
            val canReserveMore = maxLimit == null || currentReservationCount < maxLimit

            AppointmentGroupUiState(
                id = group.id,
                title = group.title,
                description = group.description,
                locationName = group.locationName,
                locationAddress = group.locationAddress,
                participantCount = group.participantCount,
                maxAppointmentsPerParticipant = group.maxAppointmentsPerParticipant,
                currentReservationCount = currentReservationCount,
                canReserveMore = canReserveMore,
                slots = group.slots.map { mapSlotToUiState(it) }
            )
        }
    }

    private fun mapSlotToUiState(slot: AppointmentSlotDomain): AppointmentSlotUiState {
        val timeRange = formatTimeRange(slot.startDate, slot.endDate)

        return AppointmentSlotUiState(
            id = slot.id,
            timeRange = timeRange,
            availableSlots = slot.availableSlots,
            isAvailable = slot.availableSlots > 0 && !slot.isReservedByMe,
            isReservedByMe = slot.isReservedByMe,
            myReservationId = slot.myReservationId,
            hasConflict = slot.conflictInfo?.hasConflict ?: false,
            conflictEventTitle = slot.conflictInfo?.conflictingEventTitle
        )
    }

    private fun formatTimeRange(startDate: java.util.Date?, endDate: java.util.Date?): String {
        if (startDate == null) return ""

        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        val datePart = dateFormat.format(startDate)
        val startTime = timeFormat.format(startDate)

        return if (endDate != null) {
            val endTime = timeFormat.format(endDate)
            "$datePart, $startTime - $endTime"
        } else {
            "$datePart, $startTime"
        }
    }
}