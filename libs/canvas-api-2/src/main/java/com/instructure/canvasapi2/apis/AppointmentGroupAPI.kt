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
package com.instructure.canvasapi2.apis

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AppointmentGroup
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag
import retrofit2.http.Url

interface AppointmentGroupAPI {

    @GET("appointment_groups")
    suspend fun getAppointmentGroups(
        @Query("scope") scope: String = "reservable",
        @Query("context_codes[]") contextCodes: List<String>,
        @Query("include[]") include: List<String> = listOf("appointments", "child_events", "available_slots", "reserved_times"),
        @Query("include_past_appointments") includePastAppointments: Boolean = false,
        @Tag restParams: RestParams
    ): DataResult<List<AppointmentGroup>>

    @GET
    suspend fun getNextPageAppointmentGroups(
        @Url nextPage: String,
        @Tag restParams: RestParams
    ): DataResult<List<AppointmentGroup>>

    @POST("calendar_events/{id}/reservations")
    suspend fun reserveAppointmentSlot(
        @Path("id") appointmentId: Long,
        @Body body: ReservationRequest
    ): ScheduleItem

    @DELETE("calendar_events/{id}")
    suspend fun cancelAppointReservation(
        @Path("id") reservationId: Long,
        @Query("cancel_reason") cancelReason: String? = null
    )

    data class ReservationRequest(
        @SerializedName("comments") val comments: String? = null
    )
}