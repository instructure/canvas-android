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
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.utils.DataResult

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Tag
import retrofit2.http.Url

object SectionAPI {

    // For section specific announcements; the api takes either 'all' or null when targeting all sections, we're using 'all'
    var ALL_SECTIONS = "all"

    interface SectionsInterface {

        @GET("courses/{courseId}/sections?include[]=total_students")
        fun getFirstPageSectionsList(@Path("courseId") courseID: Long): Call<List<Section>>

        @GET("courses/{courseId}/sections?include[]=total_students")
        suspend fun getFirstPageSectionsList(@Path("courseId") courseID: Long, @Tag params: RestParams): DataResult<List<Section>>

        @GET
        fun getNextPageSectionsList(@Url nextUrl: String): Call<List<Section>>

        @GET
        suspend fun getNextPageSectionsList(@Url nextUrl: String, @Tag params: RestParams): DataResult<List<Section>>

        @GET("courses/{courseId}/sections/{sectionId}")
        fun getSection(@Path("courseId") courseID: Long, @Path("sectionId") sectionID: Long): Call<Section>
    }

    fun getFirstSectionsForCourse(courseId: Long, adapter: RestBuilder, callback: StatusCallback<List<Section>>, params: RestParams) {
        callback.addCall(adapter.build(SectionsInterface::class.java, params).getFirstPageSectionsList(courseId)).enqueue(callback)
    }

    fun getNextPageSections(nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<Section>>, params: RestParams) {
        callback.addCall(adapter.build(SectionsInterface::class.java, params).getNextPageSectionsList(nextUrl)).enqueue(callback)
    }

    fun getSection(courseId: Long, sectionId: Long, adapter: RestBuilder, callback: StatusCallback<Section>, params: RestParams) {
        callback.addCall(adapter.build(SectionsInterface::class.java, params).getSection(courseId, sectionId)).enqueue(callback)
    }
}
