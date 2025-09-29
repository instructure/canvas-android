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
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.DataResult

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag
import retrofit2.http.Url


object ExternalToolAPI {
    // This returns a paged response, so either we have to depaginate, or pull down 100, since we're typically looking
    // for the Studio LTI.
    interface ExternalToolInterface {
        @GET("{contextId}/external_tools?include_parents=true")
        fun getExternalToolsForCanvasContext(@Path("contextId") contextId: Long): Call<List<LTITool>>

        @GET("external_tools/visible_course_nav_tools")
        fun getExternalToolsForCourses(@Query("context_codes[]", encoded = true) contextCodes: List<String>): Call<List<LTITool>>

        @GET("external_tools/visible_course_nav_tools")
        suspend fun getExternalToolsForCourses(@Query("context_codes[]", encoded = true) contextCodes: List<String>, @Tag params: RestParams): DataResult<List<LTITool>>

        @GET
        fun getLtiFromUrl(@Url url: String): Call<LTITool>
    }

    fun getExternalToolsForCanvasContext(
            canvasContextId: Long,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<List<LTITool>>) {
        callback.addCall(adapter.build(ExternalToolInterface::class.java, params).getExternalToolsForCanvasContext(canvasContextId)).enqueue(callback)
    }

    fun getExternalToolsForCourses(
        canvasContextIds: List<String>,
        adapter: RestBuilder,
        params: RestParams,
        callback: StatusCallback<List<LTITool>>) {
        callback.addCall(adapter.build(ExternalToolInterface::class.java, params).getExternalToolsForCourses(canvasContextIds)).enqueue(callback)
    }

    fun getLtiFromUrlSynchronous(url: String, adapter: RestBuilder, params: RestParams): LTITool? {
        try {
            val response = adapter.build(ExternalToolInterface::class.java, params).getLtiFromUrl(url).execute()
            return if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { }
        return null
    }
}
