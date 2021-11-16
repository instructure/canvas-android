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
 */
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.CanvaDocsAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotationResponse
import com.instructure.canvasapi2.models.canvadocs.CanvaDocSessionRequestBody
import com.instructure.canvasapi2.models.canvadocs.CanvaDocSessionResponseBody
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.apiAsync
import okhttp3.ResponseBody

object CanvaDocsManager {

    fun getCanvaDoc(previewUrl: String, callback: StatusCallback<DocSession>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(domain = ApiPrefs.fullDomain, apiVersion = "")
        CanvaDocsAPI.getCanvaDoc(previewUrl, adapter, params, callback)
    }

    fun getAnnotations(
        sessionId: String,
        canvaDocDomain: String,
        callback: StatusCallback<CanvaDocAnnotationResponse>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(domain = canvaDocDomain, apiVersion = "", isForceReadFromNetwork = true)
        CanvaDocsAPI.getAnnotations(sessionId, adapter, params, callback)
    }

    fun putAnnotation(
        sessionId: String,
        annotationId: String,
        annotation: CanvaDocAnnotation,
        canvaDocDomain: String,
        callback: StatusCallback<CanvaDocAnnotation>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(domain = canvaDocDomain, apiVersion = "")
        CanvaDocsAPI.putAnnotation(sessionId, annotationId, annotation, adapter, params, callback)
    }

    fun deleteAnnotation(
        sessionId: String,
        annotationId: String,
        canvaDocDomain: String,
        callback: StatusCallback<ResponseBody>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(domain = canvaDocDomain, apiVersion = "")
        CanvaDocsAPI.deleteAnnotation(sessionId, annotationId, adapter, params, callback)
    }

    fun createCanvaDocSessionAsync(submissionId: Long, attempt: String) = apiAsync<CanvaDocSessionResponseBody> { createCanvaDocSession(submissionId, attempt, it) }

    private fun createCanvaDocSession(
        submissionId: Long,
        attempt: String,
        callback: StatusCallback<CanvaDocSessionResponseBody>
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(domain = ApiPrefs.fullDomain)
        CanvaDocsAPI.createCanvaDocSession(
            CanvaDocSessionRequestBody(
                submissionId.toString(),
                attempt
            ), adapter, params, callback
        )
    }
}
