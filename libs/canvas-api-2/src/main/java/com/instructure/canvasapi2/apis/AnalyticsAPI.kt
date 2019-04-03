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
import com.instructure.canvasapi2.models.StudentSummary

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


object AnalyticsAPI {

    internal interface AnalyticsInterface {
        @GET("courses/{courseId}/analytics/student_summaries")
        fun getStudentSummaryForCourse(@Path("courseId") courseId: Long, @Query("student_id") studentId: Long): Call<List<StudentSummary>>
    }

    @JvmStatic
    fun getStudentSummaryForCourse(studentId: Long, courseId: Long, adapter: RestBuilder, params: RestParams, callback: StatusCallback<List<StudentSummary>>) {
        callback.addCall(adapter.build(AnalyticsInterface::class.java, params).getStudentSummaryForCourse(courseId, studentId)).enqueue(callback)
    }

}
