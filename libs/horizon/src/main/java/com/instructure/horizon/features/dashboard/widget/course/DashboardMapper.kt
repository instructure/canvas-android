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
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardButtonRoute
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardChipState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardItemState
import com.instructure.horizon.features.dashboard.widget.course.card.CardClickAction
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardImageState
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardModuleItemState
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardParentProgramState
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardState
import com.instructure.horizon.features.home.HomeNavigationRoute
import com.instructure.horizon.horizonui.molecules.StatusChipColor

internal suspend fun List<GetCoursesQuery.Enrollment>.mapToDashboardCourseCardState(
    context: Context,
    programs: List<Program>,
    nextModuleForCourse: suspend (Long?) -> DashboardCourseCardModuleItemState?
): List<DashboardCourseCardState> {
    val completed = this.filter { it.isCompleted() }.map { it.mapCompleted(context, programs) }
    val active = this.filter { it.isActive() }.map { it.mapActive(programs, nextModuleForCourse) }
    return (active + completed).sortedByDescending { course ->
        course.progress.run { if (this == 100.0) -1.0 else this } // Active courses first, then completed courses
            ?: 0.0
    }
}

internal fun List<Program>.mapToDashboardCourseCardState(context: Context): List<DashboardPaginatedWidgetCardItemState> {
    return this.mapIndexed { itemIndex, program ->
        DashboardPaginatedWidgetCardItemState(
            chipState = DashboardPaginatedWidgetCardChipState(
                label = context.getString(R.string.dashboardCourseCardProgramChipLabel),
                color = StatusChipColor.Grey
            ),
            pageState = if (size > 1) {
                context.getString(R.string.dsahboardPaginatedWidgetPagerMessage, itemIndex + 1, size)
            } else {
                null
            },
            title = context.getString(
                R.string.dashboardCourseCardProgramDetailsMessage,
                program.name
            ),
            route = DashboardPaginatedWidgetCardButtonRoute.HomeRoute(HomeNavigationRoute.Learn.withProgram(program.id)),
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

private fun GetCoursesQuery.Enrollment.mapCompleted(context: Context, programs: List<Program>): DashboardCourseCardState {
    return DashboardCourseCardState(
        parentPrograms = programs
            .filter { it.sortedRequirements.any { it.courseId == this.course?.id?.toLongOrNull() } }
            .map { program ->
                DashboardCourseCardParentProgramState(
                    programName = program.name,
                    programId = program.id,
                    onClickAction = CardClickAction.NavigateToProgram(program.id)
                )
            },
        imageState = DashboardCourseCardImageState(
            imageUrl = this.course?.image_download_url,
            showPlaceholder = true
        ),
        title = this.course?.name.orEmpty(),
        description = context.getString(R.string.dashboardCompletedCourseDetails),
        progress = this.course?.usersConnection?.nodes?.firstOrNull()?.courseProgression?.requirements?.completionPercentage ?: 0.0,
        moduleItem = null,
        buttonState = null,
        onClickAction = CardClickAction.NavigateToCourse(this.course?.id?.toLongOrNull() ?: -1L)
    )
}

private suspend fun GetCoursesQuery.Enrollment.mapActive(
    programs: List<Program>,
    nextModuleForCourse: suspend (Long?) -> DashboardCourseCardModuleItemState?
): DashboardCourseCardState {
    return DashboardCourseCardState(
        parentPrograms = programs
            .filter { it.sortedRequirements.any { it.courseId == this.course?.id?.toLongOrNull() } }
            .map { program ->
                DashboardCourseCardParentProgramState(
                    programName = program.name,
                    programId = program.id,
                    onClickAction = CardClickAction.NavigateToProgram(program.id)
                )
            },
        imageState = DashboardCourseCardImageState(
            imageUrl = this.course?.image_download_url,
            showPlaceholder = true
        ),
        title = this.course?.name.orEmpty(),
        description = null,
        progress = this.course?.usersConnection?.nodes?.firstOrNull()?.courseProgression?.requirements?.completionPercentage ?: 0.0,
        moduleItem = nextModuleForCourse(this.course?.id?.toLongOrNull()),
        buttonState = null,
        onClickAction = CardClickAction.NavigateToCourse(this.course?.id?.toLongOrNull() ?: -1L),
        lastAccessed = this.lastActivityAt
    )
}