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
 *
 */
package com.instructure.canvasapi2.models.studyassist

import com.google.gson.annotations.SerializedName

/**
 * Body for `POST /courses/:course_id/study_assist`.
 *
 * A null [prompt] is omitted by the default Gson converter, which is what asks the server for the
 * list of available tools ("chips") rather than running one.
 */
data class StudyAssistRequest(
    val prompt: String? = null,
    val state: StudyAssistState,
    val regenerate: Boolean = false,
    @SerializedName("caller_app") val callerApp: String = CALLER_APP_CANVAS
) {
    companion object {
        const val CALLER_APP_CANVAS = "canvas"
    }
}

/**
 * Identifies the content the tools run against. [pageId] is the page URL slug, not the numeric id.
 */
data class StudyAssistState(
    @SerializedName("courseID") val courseId: String,
    @SerializedName("pageID") val pageId: String
)
