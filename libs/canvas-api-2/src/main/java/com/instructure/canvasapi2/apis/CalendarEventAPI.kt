/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.canvasapi2.apis

import androidx.annotation.StringRes
import com.instructure.canvasapi2.R
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.io.IOException


object CalendarEventAPI {

    interface CalendarEventInterface {

        @get:GET("users/self/upcoming_events")
        val upcomingEvents: Call<List<ScheduleItem>>

        @GET("calendar_events/")
        fun getCalendarEvents(
                @Query("all_events") allEvents: Boolean,
                @Query("type") type: String,
                @Query("start_date") startDate: String?,
                @Query("end_date") endDate: String?,
                @Query(value = "context_codes[]", encoded = true) contextCodes: List<String>): Call<List<ScheduleItem>>

        @GET("calendar_events/")
        suspend fun getCalendarEvents(
            @Query("all_events") allEvents: Boolean,
            @Query("type") type: String,
            @Query("start_date") startDate: String?,
            @Query("end_date") endDate: String?,
            @Query(value = "context_codes[]", encoded = true) contextCodes: List<String>, @Tag restParams: RestParams
        ): DataResult<List<ScheduleItem>>

        @GET
        fun next(@Url url: String): Call<List<ScheduleItem>>

        @GET
        suspend fun next(@Url url: String, @Tag restParams: RestParams): DataResult<List<ScheduleItem>>

        @GET("calendar_events/{eventId}")
        fun getCalendarEvent(@Path("eventId") eventId: Long): Call<ScheduleItem>

        @GET("calendar_events/{eventId}?include[]=series_natural_language")
        suspend fun getCalendarEvent(@Path("eventId") eventId: Long, @Tag restParams: RestParams): DataResult<ScheduleItem>

        @DELETE("calendar_events/{eventId}")
        fun deleteCalendarEvent(@Path("eventId") eventId: Long, @Query("cancel_reason") cancelReason: String): Call<ScheduleItem>

        @DELETE("calendar_events/{eventId}")
        suspend fun deleteRecurringCalendarEvent(
            @Path("eventId") eventId: Long,
            @Query("which") deleteScope: String,
            @Tag restParams: RestParams
        ): DataResult<List<ScheduleItem>>

        @DELETE("calendar_events/{eventId}")
        suspend fun deleteCalendarEvent(
            @Path("eventId") eventId: Long,
            @Tag restParams: RestParams
        ): DataResult<ScheduleItem>

        @POST("calendar_events/")
        fun createCalendarEvent(
                @Query("calendar_event[context_code]") contextCode: String,
                @Query(value = "calendar_event[title]", encoded = true) title: String,
                @Query(value = "calendar_event[description]", encoded = true) description: String,
                @Query("calendar_event[start_at]") startDate: String,
                @Query("calendar_event[end_at]") endDate: String,
                @Query(value = "calendar_event[location_name]", encoded = true) locationName: String,
                @Body body: String): Call<ScheduleItem>

        @POST("calendar_events/")
        suspend fun createCalendarEvent(
            @Body body: ScheduleItem.ScheduleItemParamsWrapper,
            @Tag restParams: RestParams
        ): DataResult<ScheduleItem>

        @PUT("calendar_events/{eventId}")
        suspend fun updateRecurringCalendarEvent(
            @Path("eventId") eventId: Long,
            @Query(value = "which") modifyEventScope: String,
            @Body body: ScheduleItem.ScheduleItemParamsWrapper,
            @Tag restParams: RestParams
        ): DataResult<List<ScheduleItem>>

        @PUT("calendar_events/{eventId}")
        suspend fun updateRecurringCalendarEventOneOccurrence(
            @Path("eventId") eventId: Long,
            @Query(value = "which") modifyEventScope: String,
            @Body body: ScheduleItem.ScheduleItemParamsWrapper,
            @Tag restParams: RestParams
        ): DataResult<ScheduleItem>

        @PUT("calendar_events/{eventId}")
        suspend fun updateCalendarEvent(
            @Path("eventId") eventId: Long,
            @Body body: ScheduleItem.ScheduleItemParamsWrapper,
            @Tag restParams: RestParams
        ): DataResult<ScheduleItem>

        @GET("calendar_events/")
        fun getImportantDates(
                @Query("start_date") startDate: String?,
                @Query("end_date") endDate: String?,
                @Query("type") type: String,
                @Query(value = "context_codes[]", encoded = true) contextCodes: List<String>,
                @Query("important_dates") importantDates: Boolean = true): Call<List<ScheduleItem>>
    }

    enum class CalendarEventType(val apiName: String) {
        CALENDAR("event"),
        ASSIGNMENT("assignment"),
        SUB_ASSIGNMENT("sub_assignment")
    }

    enum class ModifyEventScope(val apiName: String, @StringRes val stringRes: Int) {
        ONE("one", R.string.eventDeleteScopeOne),
        ALL("all", R.string.eventDeleteScopeAll),
        FOLLOWING("following", R.string.eventDeleteScopeFollowing)
    }

    fun getCalendarEvent(
            eventId: Long,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<ScheduleItem>) {
        callback.addCall(adapter.build(CalendarEventInterface::class.java, params).getCalendarEvent(eventId)).enqueue(callback)
    }

    fun getCalendarEvents(
            allEvents: Boolean,
            type: CalendarEventType,
            startDate: String?,
            endDate: String?,
            canvasContexts: List<String>,
            adapter: RestBuilder,
            callback: StatusCallback<List<ScheduleItem>>,
            params: RestParams) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(CalendarEventInterface::class.java, params)
                    .getCalendarEvents(allEvents, type.apiName, startDate, endDate, canvasContexts)).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            callback.addCall(adapter.build(CalendarEventInterface::class.java, params)
                    .next(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    fun getImportantDates(
            startDate: String?,
            endDate: String?,
            type: CalendarEventType,
            canvasContexts: List<String>,
            adapter: RestBuilder,
            callback: StatusCallback<List<ScheduleItem>>,
            params: RestParams) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(CalendarEventInterface::class.java, params)
                    .getImportantDates(startDate, endDate, type.apiName, canvasContexts)).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            callback.addCall(adapter.build(CalendarEventInterface::class.java, params)
                    .next(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    @Throws(IOException::class)
    fun getUpcomingEventsSynchronous(
            adapter: RestBuilder,
            params: RestParams): Response<List<ScheduleItem>> {
        return adapter.build(CalendarEventInterface::class.java, params).upcomingEvents.execute()
    }

    fun deleteCalendarEvent(
            eventId: Long,
            cancelReason: String,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<ScheduleItem>) {
        callback.addCall(adapter.build(CalendarEventInterface::class.java, params).deleteCalendarEvent(eventId, cancelReason)).enqueue(callback)
    }

    fun createCalendarEvent(
            contextCode: String,
            title: String,
            description: String,
            startDate: String,
            endDate: String,
            location: String,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<ScheduleItem>) {
        val call = adapter.build(CalendarEventInterface::class.java, params).createCalendarEvent(contextCode, title, description, startDate, endDate, location, "")
        callback.addCall(call).enqueue(callback)
    }
}

