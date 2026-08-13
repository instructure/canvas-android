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
package com.instructure.pandautils.domain.usecase.studytools

import com.instructure.pandautils.data.repository.studytools.StudyToolsRepository
import com.instructure.pandautils.domain.models.studytools.StudyTool
import com.instructure.pandautils.domain.models.studytools.StudyToolResult
import com.instructure.pandautils.domain.models.studytools.StudyToolsOutcome
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * Generates the content for a single Study Tool.
 *
 * Callers must pass `regenerate = true` for a "Regenerate" action, otherwise the server returns the
 * result it cached for the previous identical request.
 */
class RunStudyToolUseCase @Inject constructor(
    private val studyToolsRepository: StudyToolsRepository,
    private val errorMapper: StudyToolsErrorMapper
) : BaseUseCase<RunStudyToolUseCase.Params, StudyToolsOutcome<StudyToolResult>>() {

    override suspend fun execute(params: Params): StudyToolsOutcome<StudyToolResult> {
        return errorMapper.toOutcome(
            studyToolsRepository.runTool(
                courseId = params.courseId,
                pageUrl = params.pageUrl,
                tool = params.tool,
                prompt = params.prompt,
                regenerate = params.regenerate
            )
        )
    }

    data class Params(
        val courseId: Long,
        val pageUrl: String,
        val tool: StudyTool,
        val prompt: String,
        val regenerate: Boolean = false
    )
}
