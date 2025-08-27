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
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotationResponse
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.models.canvadocs.CanvaDocSessionRequestBody
import com.instructure.canvasapi2.models.canvadocs.CanvaDocSessionResponseBody
import com.instructure.canvasapi2.utils.DataResult

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

object CanvaDocsAPI {

    interface CanvaDocsInterFace {
        @GET
        fun getCanvaDoc(@Url url: String): Call<DocSession>

        @GET("/2018-04-06/sessions/{sessionId}/annotations")
        fun getAnnotations(@Path("sessionId") sessionId: String): Call<CanvaDocAnnotationResponse>

        @PUT("/2018-03-07/sessions/{sessionId}/annotations/{annotationId}")
        fun putAnnotation(@Path("sessionId") sessionId: String, @Path("annotationId") annotationId: String, @Body annotation: CanvaDocAnnotation): Call<CanvaDocAnnotation>

        @DELETE("/1/sessions/{sessionId}/annotations/{annotationId}")
        fun deleteAnnotation(@Path("sessionId") sessionId: String, @Path("annotationId") annotationId: String): Call<ResponseBody>

        @POST("canvadoc_session")
        fun createCanvaDocSession(@Body body: CanvaDocSessionRequestBody): Call<CanvaDocSessionResponseBody>

        @POST("canvadoc_session")
        suspend fun createCanvaDocSession(@Body body: CanvaDocSessionRequestBody, @Tag restParams: RestParams): DataResult<CanvaDocSessionResponseBody>
    }

    fun getCanvaDoc(
            previewUrl: String,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<DocSession>) {
        callback.addCall(adapter.build(CanvaDocsInterFace::class.java, params).getCanvaDoc(previewUrl)).enqueue(callback)
    }

    fun getAnnotations(
            sessionId: String,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<CanvaDocAnnotationResponse>) {
        callback.addCall(adapter.build(CanvaDocsInterFace::class.java, params).getAnnotations(sessionId)).enqueue(callback)
    }

    fun putAnnotation(
            sessionId: String,
            annotationId: String,
            annotation: CanvaDocAnnotation,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<CanvaDocAnnotation>) {
        callback.addCall(adapter.build(CanvaDocsInterFace::class.java, params).putAnnotation(sessionId, annotationId, annotation)).enqueue(callback)
    }

    fun deleteAnnotation(
            sessionId: String,
            annotationId: String,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<ResponseBody>) {
        callback.addCall(adapter.build(CanvaDocsInterFace::class.java, params).deleteAnnotation(sessionId, annotationId)).enqueue(callback)
    }

    fun createCanvaDocSession(
        body: CanvaDocSessionRequestBody,
        adapter: RestBuilder,
        params: RestParams,
        callback: StatusCallback<CanvaDocSessionResponseBody>) {
        callback.addCall(adapter.build(CanvaDocsInterFace::class.java, params).createCanvaDocSession(body)).enqueue(callback)
    }
}
