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
package com.instructure.pandautils.domain.models.studytools

/**
 * The Study Assist failures the UI needs to tell apart.
 *
 * [message] and [header] come from the server's error payload and are already localized, so callers
 * should prefer them over local copy and only fall back to a generic string when they are null.
 */
sealed class StudyToolsError(
    open val message: String? = null,
    open val header: String? = null
) {
    /**
     * The feature or the individual tool is not available for this course or user (404/403/401).
     * The entry point should be hidden rather than showing an error.
     */
    data class Unavailable(override val message: String? = null) : StudyToolsError(message)

    /** The daily generation limit was reached (429). Not retryable. */
    data class RateLimited(override val message: String? = null) : StudyToolsError(message)

    /**
     * The content could not be turned into a result — unreadable, too large, unsupported type, or
     * a malformed model response (422). Retrying the same content will not help.
     */
    data class ContentProblem(
        override val message: String? = null,
        override val header: String? = null
    ) : StudyToolsError(message, header)

    /**
     * Upstream generation is temporarily down (502/503) or the request itself failed. Retryable,
     * and the repository has already exhausted its automatic retries by the time this surfaces.
     */
    data class TemporarilyUnavailable(override val message: String? = null) : StudyToolsError(message)
}
