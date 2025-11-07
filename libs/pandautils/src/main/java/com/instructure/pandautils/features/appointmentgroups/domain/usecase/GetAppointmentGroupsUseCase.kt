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

import com.instructure.canvasapi2.models.AppointmentGroupDomain
import com.instructure.canvasapi2.models.AppointmentSlotDomain
import com.instructure.canvasapi2.models.ConflictInfo
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.features.appointmentgroups.AppointmentGroupRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class GetAppointmentGroupsUseCase @Inject constructor(
    private val repository: AppointmentGroupRepository,
    private val apiPrefs: ApiPrefs
) : UseCase<GetAppointmentGroupsUseCase.Params, List<AppointmentGroupDomain>>() {

    override suspend fun execute(params: Params): DataResult<List<AppointmentGroupDomain>> {
        val appointmentGroupsResult = repository.getAppointmentGroups(
            courseIds = params.courseIds,
            includePastAppointments = params.includePastAppointments,
            forceNetwork = params.forceNetwork
        )
        if (appointmentGroupsResult is DataResult.Fail) {
            return DataResult.Fail()
        }

        val appointmentGroups = (appointmentGroupsResult as DataResult.Success).data

        val (startDate, endDate) = calculateDateRange(appointmentGroups)
        val userEventsResult = repository.getUserEvents(startDate, endDate, params.forceNetwork)
        val userEvents = (userEventsResult as? DataResult.Success)?.data ?: emptyList()

        val domainGroups = appointmentGroups.map { group ->
            AppointmentGroupDomain(
                id = group.id,
                title = group.title,
                description = group.description,
                locationName = group.locationName,
                locationAddress = group.locationAddress,
                participantCount = group.participantCount,
                maxAppointmentsPerParticipant = group.maxAppointmentsPerParticipant,
                slots = group.appointments.map { slot ->
                    val conflictInfo = checkForConflict(slot.startAt, slot.endAt, userEvents)
                    AppointmentSlotDomain(
                        id = slot.id,
                        appointmentGroupId = slot.appointmentGroupId,
                        startDate = slot.startDate,
                        endDate = slot.endDate,
                        availableSlots = slot.availableSlots,
                        isReservedByMe = slot.isReservedByMe,
                        myReservationId = slot.myReservation?.id,
                        conflictInfo = conflictInfo
                    )
                }
            )
        }

        return DataResult.Success(domainGroups)
    }

    private fun calculateDateRange(appointmentGroups: List<com.instructure.canvasapi2.models.AppointmentGroup>): Pair<String, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

        val allDates = appointmentGroups.flatMap { group ->
            group.appointments.mapNotNull { it.startDate }
        }

        if (allDates.isEmpty()) {
            val now = Calendar.getInstance()
            val end = Calendar.getInstance().apply { add(Calendar.MONTH, 3) }
            return Pair(dateFormat.format(now.time), dateFormat.format(end.time))
        }

        val minDate = allDates.minOrNull() ?: Calendar.getInstance().time
        val maxDate = allDates.maxOrNull() ?: Calendar.getInstance().apply { add(Calendar.MONTH, 3) }.time

        return Pair(dateFormat.format(minDate), dateFormat.format(maxDate))
    }

    private fun checkForConflict(
        slotStartAt: String?,
        slotEndAt: String?,
        userEvents: List<PlannerItem>
    ): ConflictInfo {
        if (slotStartAt == null || slotEndAt == null) {
            return ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        }

        val slotStart = slotStartAt.toDate()?.time ?: return ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        val slotEnd = slotEndAt.toDate()?.time ?: return ConflictInfo(hasConflict = false, conflictingEventTitle = null)

        val conflictingEvent = userEvents.find { event ->
            val eventStart = event.plannableDate.time
            val eventEnd = event.plannable.endAt?.time ?: (eventStart + 3600000)

            (slotStart < eventEnd && slotEnd > eventStart)
        }

        return if (conflictingEvent != null) {
            ConflictInfo(hasConflict = true, conflictingEventTitle = conflictingEvent.plannable.title)
        } else {
            ConflictInfo(hasConflict = false, conflictingEventTitle = null)
        }
    }

    data class Params(
        val courseIds: List<Long>,
        val includePastAppointments: Boolean = false,
        val forceNetwork: Boolean = false
    )
}