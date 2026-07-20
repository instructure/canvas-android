/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.data.repository.canvadocs

import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotationResponse
import com.instructure.canvasapi2.models.canvadocs.CanvaDocSessionResponseBody
import com.instructure.canvasapi2.utils.DataResult

/**
 * Reads a CanvaDoc submission session and its annotations. The document/annotation endpoints live on
 * the CanvaDocs domain (not the Canvas domain), so [getCanvaDoc]/[getAnnotations] target that domain.
 */
interface CanvaDocsRepository {

    suspend fun createCanvaDocSession(submissionId: Long, attempt: String): DataResult<CanvaDocSessionResponseBody>

    suspend fun getCanvaDoc(previewUrl: String): DataResult<DocSession>

    suspend fun getAnnotations(sessionId: String, canvaDocDomain: String): DataResult<CanvaDocAnnotationResponse>
}
