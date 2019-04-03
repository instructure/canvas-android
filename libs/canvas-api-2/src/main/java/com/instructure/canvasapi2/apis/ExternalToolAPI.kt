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

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url


internal object ExternalToolAPI {

    internal interface ExternalToolInterface {
        @GET("{contextId}/external_tools?include_parents=true")
        fun getExternalToolsForCanvasContext(@Path("contextId") contextId: Long): Call<List<LTITool>>

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

    fun getLtiFromUrlSynchronous(url: String, adapter: RestBuilder, params: RestParams): LTITool? {
        try {
            val response = adapter.build(ExternalToolInterface::class.java, params).getLtiFromUrl(url).execute()
            return if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { }
        return null
    }
}
