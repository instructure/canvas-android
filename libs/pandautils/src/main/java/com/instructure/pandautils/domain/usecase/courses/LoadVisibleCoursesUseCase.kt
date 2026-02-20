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

package com.instructure.pandautils.domain.usecase.courses

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class LoadVisibleCoursesUseCase @Inject constructor(
    private val loadAllCoursesUseCase: LoadAllCoursesUseCase,
    private val loadDashboardCardsUseCase: LoadDashboardCardsUseCase
) : BaseUseCase<LoadVisibleCoursesUseCase.Params, LoadVisibleCoursesUseCase.Result>() {

    override suspend fun execute(params: Params): Result {
        val allCourses = loadAllCoursesUseCase(LoadAllCoursesUseCase.Params(params.forceRefresh))
        val dashboardCards = loadDashboardCardsUseCase(LoadDashboardCardsUseCase.Params(params.forceRefresh))

        val coursesMap = allCourses.associateBy { it.id }

        val visibleCourses = dashboardCards
            .map { card -> courseFromDashboardCard(card, coursesMap) }
            .sortedBy { course -> dashboardCards.find { it.id == course.id }?.position ?: Int.MAX_VALUE }

        return Result(visibleCourses = visibleCourses, allCourses = allCourses)
    }

    private fun courseFromDashboardCard(card: DashboardCard, coursesMap: Map<Long, Course>): Course {
        return coursesMap[card.id]
            ?: Course(
                id = card.id,
                name = card.shortName ?: card.originalName.orEmpty(),
                originalName = card.originalName,
                courseCode = card.courseCode
            )
    }

    data class Params(
        val forceRefresh: Boolean = false
    )

    data class Result(
        val visibleCourses: List<Course>,
        val allCourses: List<Course>
    )
}