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
package com.instructure.pandautils.data.repository.studytools

import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.domain.models.studytools.StudyTool
import com.instructure.pandautils.domain.models.studytools.StudyToolOption
import com.instructure.pandautils.domain.models.studytools.StudyToolResult

interface StudyToolsRepository {

    /**
     * Asks the server which tools are available for a page. An empty list means the feature is not
     * usable here and the entry point should stay hidden — the server filters the list by the
     * per-tool feature flags.
     */
    suspend fun getAvailableTools(
        courseId: Long,
        pageUrl: String
    ): DataResult<List<StudyToolOption>>

    /**
     * Runs a single tool. [prompt] must be the value echoed from [StudyToolOption.prompt].
     *
     * [regenerate] must be true for a "Regenerate" action: results are cached server-side for 24
     * hours and an otherwise identical request returns the same content.
     */
    suspend fun runTool(
        courseId: Long,
        pageUrl: String,
        tool: StudyTool,
        prompt: String,
        regenerate: Boolean = false
    ): DataResult<StudyToolResult>
}
