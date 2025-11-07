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
package com.instructure.pandautils.features.appointmentgroups

import com.instructure.canvasapi2.models.AppointmentSlotDomain
import com.instructure.canvasapi2.models.AppointmentGroupDomain
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendar.EventUiState
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

object AppointmentGroupsCalendarMapper {

    fun mapAppointmentSlotToEventUiState(
        slot: AppointmentSlotDomain,
        appointmentGroup: AppointmentGroupDomain,
        canvasContext: CanvasContext
    ): EventUiState {
        val startTime = if (slot.startDate != null) {
            try {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
                timeFormat.format(slot.startDate)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        return EventUiState(
            plannableId = slot.id,
            contextName = appointmentGroup.title,
            canvasContext = canvasContext,
            name = appointmentGroup.title,
            iconRes = R.drawable.ic_appointment,
            date = startTime,
            status = if (slot.isReservedByMe) "Reserved" else "Available",
            tag = appointmentGroup.title,
            isReservation = slot.isReservedByMe,
            reservationId = slot.myReservationId,
            appointmentGroupId = appointmentGroup.id,
            canCancel = slot.isReservedByMe
        )
    }

    fun mapAppointmentGroupsToEventUiStates(
        appointmentGroups: List<AppointmentGroupDomain>,
        canvasContext: CanvasContext
    ): List<EventUiState> {
        return appointmentGroups
            .flatMap { group ->
                group.slots.map { slot ->
                    mapAppointmentSlotToEventUiState(slot, group, canvasContext)
                }
            }
    }
}