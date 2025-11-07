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

import com.instructure.canvasapi2.apis.AppointmentGroupAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AppointmentGroup
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class AppointmentGroupRepository @Inject constructor(
    private val appointmentGroupApi: AppointmentGroupAPI,
    private val plannerApi: PlannerAPI.PlannerInterface
) {

    suspend fun getAppointmentGroups(
        courseIds: List<Long>,
        includePastAppointments: Boolean = false,
        forceNetwork: Boolean = false
    ): DataResult<List<AppointmentGroup>> {
        return try {
            val params = RestParams(isForceReadFromNetwork = forceNetwork)
            val contextCodes = courseIds.map { "course_$it" }
            appointmentGroupApi.getAppointmentGroups(
                contextCodes = contextCodes,
                includePastAppointments = includePastAppointments,
                restParams = params
            ).depaginate {
                appointmentGroupApi.getNextPageAppointmentGroups(
                    it,
                    params
                )
            }
        } catch (e: Exception) {
            DataResult.Fail()
        }
    }

    suspend fun getUserEvents(startDate: String, endDate: String, forceNetwork: Boolean = false): DataResult<List<PlannerItem>> {
        return plannerApi.getPlannerItems(
            startDate = startDate,
            endDate = endDate,
            contextCodes = emptyList(),
            filter = null,
            restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        )
    }

    suspend fun reserveSlot(
        appointmentId: Long,
        comments: String?
    ): DataResult<ScheduleItem> {
        return try {
            val body = AppointmentGroupAPI.ReservationRequest(comments)
            val result = appointmentGroupApi.reserveAppointmentSlot(appointmentId, body)
            DataResult.Success(result)
        } catch (e: Exception) {
            DataResult.Fail()
        }
    }

    suspend fun cancelReservation(
        reservationId: Long
    ): DataResult<Unit> {
        return try {
            appointmentGroupApi.cancelAppointReservation(reservationId)
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Fail()
        }
    }
}