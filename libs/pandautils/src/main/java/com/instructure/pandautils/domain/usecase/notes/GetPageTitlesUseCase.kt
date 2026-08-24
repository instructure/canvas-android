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

import com.instructure.pandautils.data.repository.page.PageRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * A `page id -> title` lookup for [Params.courseId] - used only to label the Notebook Course level's
 * section headers, which group notes by page but a [com.instructure.canvasapi2.models.StudyNote] itself
 * only carries a `learningObjectId`, never a title. Always reads cache-first - a course's page titles
 * rarely change, so there's no need to force a network round-trip here.
 */
class GetPageTitlesUseCase @Inject constructor(
    private val pageRepository: PageRepository
) : BaseUseCase<GetPageTitlesUseCase.Params, Map<String, String>>() {

    override suspend fun execute(params: Params): Map<String, String> {
        val pages = pageRepository.getCoursePages(params.courseId, forceRefresh = false).dataOrThrow
        return pages
            .filter { it.id != 0L && !it.title.isNullOrBlank() }
            .associate { it.id.toString() to it.title!! }
    }

    data class Params(
        val courseId: Long
    )
}
