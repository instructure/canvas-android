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

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Attendance

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

object AttendanceAPI {
    const val BASE_DOMAIN = "rollcall.instructure.com"
    const val BASE_TEST_DOMAIN = "rollcall-beta.instructure.com"

    internal interface AttendanceInterface {

        @GET("statuses")
        fun getAttendance(
                @Query("section_id") sectionId: Long,
                @Query("class_date") date: String,
                @Header("X-CSRF-Token") token: String,
                @Header("Cookie") cookie: String): Call<List<Attendance>>

        @POST("statuses")
        fun postAttendance(
                @Body body: Attendance,
                @Header("X-CSRF-Token") token: String,
                @Header("Cookie") cookie: String): Call<Attendance>

        @PUT("statuses/{statusId}")
        fun putAttendance(
                @Path("statusId") statusId: Long,
                @Body body: Attendance,
                @Header("X-CSRF-Token") token: String,
                @Header("Cookie") cookie: String): Call<Attendance>

        @DELETE("statuses/{statusId}")
        fun deleteAttendance(
                @Path("statusId") statusId: Long,
                @Header("X-CSRF-Token") token: String,
                @Header("Cookie") cookie: String): Call<Attendance>
    }

    /**
     * Get attendance objects for a course
     * @param sectionId A section ID for a course. It is expected the use can view this section and that work has been done client side.
     * @param date A valid Calendar object. This is transformed into a yyyy-MM-dd format
     * @param token The CSRF token which is retrieved from the LTI Launch HTML response.
     * @param cookie The cookie which is retrieved from the LTI Launch
     * @param adapter RestBuilder
     * @param callback StatusCallback
     * @param params RestParams
     */
    fun getAttendance(sectionId: Long,
                      date: Calendar,
                      token: String,
                      cookie: String,
                      adapter: RestBuilder,
                      callback: StatusCallback<List<Attendance>>,
                      params: RestParams) {

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formatter.timeZone = date.timeZone
        callback.addCall(adapter.buildRollCall(AttendanceInterface::class.java, params)
                .getAttendance(sectionId, formatter.format(date.time), token, cookie)).enqueue(callback)
    }

    /**
     * Marks attendance for a particular student. It is expected that the state passed in is the desired state to be posted.
     * @param attendance An attendance object with the expected state. Only exception is the status id should remain until after a DELETE is complete then be removed.
     * @param token The CSRF token which is retrieved from the LTI Launch HTML response.
     * @param cookie The cookie which is retrieved from the LTI Launch
     * @param adapter RestBuilder
     * @param callback StatusCallback
     * @param params RestParams
     */
    fun markAttendance(attendance: Attendance,
                       token: String,
                       cookie: String,
                       adapter: RestBuilder,
                       callback: StatusCallback<Attendance>,
                       params: RestParams) {
        //if status id == null -> POST
        //if status id != null -> 1. PUT to update 2. DELETE to unmark

        if (attendance.statusId == null) { // Unmarked at the moment
            // POST (initial post of attendance to an unmarked student)
            callback.addCall(adapter.buildRollCall(AttendanceInterface::class.java, params)
                    .postAttendance(attendance, token, cookie)).enqueue(callback)
        } else {
            if (attendance.attendanceStatus() === Attendance.Attendance.UNMARKED) {
                // DELETE (unmarking a student who has attendance marked)
                callback.addCall(adapter.buildRollCall(AttendanceInterface::class.java, params)
                        .deleteAttendance(attendance.statusId!!, token, cookie)).enqueue(callback)
            } else {
                // PUT (updating attendance from current status to another status that is not unmarked)
                callback.addCall(adapter.buildRollCall(AttendanceInterface::class.java, params)
                        .putAttendance(attendance.statusId!!, attendance, token, cookie)).enqueue(callback)
            }
        }
    }
}
