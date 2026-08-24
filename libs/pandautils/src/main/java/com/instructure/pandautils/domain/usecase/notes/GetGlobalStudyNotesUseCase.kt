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
package com.instructure.pandautils.domain.usecase.notes

import com.instructure.canvasapi2.managers.graphql.StudyNoteManager
import com.instructure.canvasapi2.models.CourseTaggedStudyNote
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * The current user's notes across every course in [Params.courseIds], each tagged with the course it
 * came from. Params takes the course id list explicitly (rather than resolving it itself) so the caller
 * - [com.instructure.pandautils.features.notes.notebook.NotebookViewModel] - can reuse whichever course
 * list it already fetched (via [GetEligibleCoursesUseCase]) instead of this use case fetching its own.
 */
class GetGlobalStudyNotesUseCase @Inject constructor(
    private val studyNoteManager: StudyNoteManager
) : BaseUseCase<GetGlobalStudyNotesUseCase.Params, List<CourseTaggedStudyNote>>() {

    override suspend fun execute(params: Params): List<CourseTaggedStudyNote> {
        return studyNoteManager.getGlobalStudyNotes(params.courseIds, forceNetwork = params.forceNetwork)
    }

    data class Params(
        val courseIds: List<Long>,
        val forceNetwork: Boolean = true
    )
}
