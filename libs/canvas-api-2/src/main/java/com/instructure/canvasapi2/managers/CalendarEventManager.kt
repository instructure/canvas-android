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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.ExhaustiveListCallback
import java.io.IOException

object CalendarEventManager {

    @JvmStatic
    fun getCalendarEventsExhaustive(
        allEvents: Boolean,
        type: CalendarEventAPI.CalendarEventType,
        startDate: String?,
        endDate: String?,
        canvasContexts: List<String>,
        callback: StatusCallback<List<ScheduleItem>>,
        forceNetwork: Boolean
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val depaginatedCallback = object : ExhaustiveListCallback<ScheduleItem>(callback) {
            override fun getNextPage(callback: StatusCallback<List<ScheduleItem>>, nextUrl: String, isCached: Boolean) {
                CalendarEventAPI.getCalendarEvents(
                    allEvents,
                    type,
                    startDate,
                    endDate,
                    canvasContexts,
                    adapter,
                    callback,
                    params
                )
            }
        }

        adapter.statusCallback = depaginatedCallback
        CalendarEventAPI.getCalendarEvents(
            allEvents,
            type,
            startDate,
            endDate,
            canvasContexts,
            adapter,
            depaginatedCallback,
            params
        )
    }

    @Throws(IOException::class)
    @JvmStatic
    fun getUpcomingEventsSynchronous(forceNetwork: Boolean): List<ScheduleItem> {
        val adapter = RestBuilder()
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        val response = CalendarEventAPI.getUpcomingEventsSynchronous(adapter, params)

        return response.body()?.let { if (response.isSuccessful) it else emptyList() } ?: emptyList()
    }

    @JvmStatic
    fun getCalendarEvent(eventId: Long, callback: StatusCallback<ScheduleItem>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        CalendarEventAPI.getCalendarEvent(eventId, adapter, params, callback)
    }

    @JvmStatic
    fun deleteCalendarEvent(eventId: Long, cancelReason: String, callback: StatusCallback<ScheduleItem>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        CalendarEventAPI.deleteCalendarEvent(eventId, cancelReason, adapter, params, callback)
    }

    @JvmStatic
    fun createCalendarEvent(
        contextCode: String,
        title: String,
        description: String,
        startDate: String,
        endDate: String,
        location: String,
        callback: StatusCallback<ScheduleItem>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams()

        CalendarEventAPI.createCalendarEvent(
            contextCode,
            title,
            description,
            startDate,
            endDate,
            location,
            adapter,
            params,
            callback
        )
    }

}
