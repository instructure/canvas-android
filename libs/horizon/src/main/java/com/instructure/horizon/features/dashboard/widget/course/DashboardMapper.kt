/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.dashboard.widget.course

import android.content.Context
import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.type.EnrollmentWorkflowState
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardHeaderState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetPageState
import com.instructure.horizon.features.dashboard.widget.course.card.CardClickAction
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardDescriptionState
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardImageState
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardModuleItemState
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardParentProgramState
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardState
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors

internal suspend fun List<GetCoursesQuery.Enrollment>.mapToDashboardCourseCardState(
    context: Context,
    programs: List<Program>,
    nextModuleForCourse: suspend (Long?) -> DashboardCourseCardModuleItemState?
): List<DashboardCourseCardState> {
    val completed = this.filter { it.isCompleted() }.mapCompleted(context, programs)
    val active = this.filter { it.isActive() }.mapActive(programs, nextModuleForCourse)
    return (active + completed).adjustAndSortCourseCardValues()
}

internal fun List<Program>.mapToDashboardCourseCardState(context: Context): List<DashboardPaginatedWidgetCardItemState> {
    return this.map { program ->
        DashboardPaginatedWidgetCardItemState(
            headerState = DashboardPaginatedWidgetCardHeaderState(
                label = context.getString(R.string.dashboardCourseCardProgramChipLabel),
                color = HorizonColors.Surface.institution().copy(0.1f),
                iconRes = R.drawable.book_2
            ),
            title = context.getString(
                R.string.dashboardCourseCardProgramDetailsMessage,
                program.name
            ),
            route = LearnRoute.LearnProgramDetailsScreen.route(program.id),
        )
    }
}

private fun GetCoursesQuery.Enrollment.isCompleted(): Boolean {
    return this.state == EnrollmentWorkflowState.completed || this.isMaxProgress()
}

private fun GetCoursesQuery.Enrollment.isActive(): Boolean {
    return this.state == EnrollmentWorkflowState.active && !this.isMaxProgress()
}

private fun GetCoursesQuery.Enrollment.isMaxProgress(): Boolean {
    return this.course?.usersConnection?.nodes?.firstOrNull()?.courseProgression?.requirements?.completionPercentage == 100.0
}

private fun List<GetCoursesQuery.Enrollment>.mapCompleted(context: Context, programs: List<Program>): List<DashboardCourseCardState> {
    return map { item ->
        DashboardCourseCardState(
            parentPrograms = programs
                .filter { it.sortedRequirements.any { it.courseId == item.course?.id?.toLongOrNull() } }
                .map { program ->
                    DashboardCourseCardParentProgramState(
                        programName = program.name,
                        programId = program.id,
                        onClickAction = CardClickAction.NavigateToProgram(program.id)
                    )
                },
            imageState = DashboardCourseCardImageState(
                imageUrl = item.course?.image_download_url,
                showPlaceholder = true
            ),
            title = item.course?.name.orEmpty(),
            descriptionState = DashboardCourseCardDescriptionState(
                descriptionTitle = context.getString(R.string.dashboardCompletedCourseTitle),
                description = context.getString(R.string.dashboardCompletedCourseMessage),
            ),
            progress = item.course?.usersConnection?.nodes?.firstOrNull()?.courseProgression?.requirements?.completionPercentage
                ?: 0.0,
            moduleItem = null,
            onClickAction = CardClickAction.NavigateToCourse(item.course?.id?.toLongOrNull() ?: -1L)
        )
    }
}

private suspend fun List<GetCoursesQuery.Enrollment>.mapActive(
    programs: List<Program>,
    nextModuleForCourse: suspend (Long?) -> DashboardCourseCardModuleItemState?
): List<DashboardCourseCardState> {
    return map { item ->
        DashboardCourseCardState(
            parentPrograms = programs
                .filter { it.sortedRequirements.any { it.courseId == item.course?.id?.toLongOrNull() } }
                .map { program ->
                    DashboardCourseCardParentProgramState(
                        programName = program.name,
                        programId = program.id,
                        onClickAction = CardClickAction.NavigateToProgram(program.id)
                    )
                },
            imageState = DashboardCourseCardImageState(
                imageUrl = item.course?.image_download_url,
                showPlaceholder = true
            ),
            title = item.course?.name.orEmpty(),
            descriptionState = null,
            progress = item.course?.usersConnection?.nodes?.firstOrNull()?.courseProgression?.requirements?.completionPercentage
                ?: 0.0,
            moduleItem = nextModuleForCourse(item.course?.id?.toLongOrNull()),
            onClickAction = CardClickAction.NavigateToCourse(
                item.course?.id?.toLongOrNull() ?: -1L
            ),
        )
    }
}

private fun List<DashboardCourseCardState>.adjustAndSortCourseCardValues(): List<DashboardCourseCardState> {
    return sortedByDescending { course ->
        course.progress.run { if (this == 100.0) -1.0 else this } // Active courses first, then completed courses
            ?: 0.0
    }.mapIndexed { index, item ->
        item.copy(
            pageState = DashboardWidgetPageState(
                currentPageNumber = index + 1,
                pageCount = size
            )
        )
    }
}