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
import com.instructure.pandautils.domain.models.studytools.StudyToolOption
import com.instructure.pandautils.domain.models.studytools.StudyToolsOutcome
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * Returns the Study Tools available for a page. An empty list means the entry point should be
 * hidden — the server filters the list by the per-tool feature flags.
 */
class GetStudyToolsUseCase @Inject constructor(
    private val studyToolsRepository: StudyToolsRepository,
    private val errorMapper: StudyToolsErrorMapper
) : BaseUseCase<GetStudyToolsUseCase.Params, StudyToolsOutcome<List<StudyToolOption>>>() {

    override suspend fun execute(params: Params): StudyToolsOutcome<List<StudyToolOption>> {
        return errorMapper.toOutcome(
            studyToolsRepository.getAvailableTools(params.courseId, params.pageUrl)
        )
    }

    data class Params(
        val courseId: Long,
        val pageUrl: String
    )
}
