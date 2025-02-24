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
 *
 */

package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.ScheduleItemApiModel
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

object CalendarEventApi {

    interface CalendarEventService {
        @POST("calendar_events/")
        fun createCalendarEvent(
            @Query("calendar_event[context_code]") contextCode: String,
            @Query(value = "calendar_event[title]", encoded = true) title: String,
            @Query("calendar_event[start_at]") startDate: String
        ): Call<ScheduleItemApiModel>
    }

    private fun calendarEventService(token: String): CalendarEventService {
        return CanvasNetworkAdapter.retrofitWithToken(token).create(CalendarEventService::class.java)
    }

    fun createCalendarEvent(token: String, contextCode: String, title: String, startDate: String): ScheduleItemApiModel = calendarEventService(token)
        .createCalendarEvent(contextCode, title, startDate)
        .execute()
        .body()!!
}
