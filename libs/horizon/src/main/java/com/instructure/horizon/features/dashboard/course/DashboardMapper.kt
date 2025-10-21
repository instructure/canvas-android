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
package com.instructure.horizon.features.dashboard.course

import android.content.Context
import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.type.EnrollmentWorkflowState
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.course.card.CardClickAction
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardButtonState
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardChipState
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardImageState
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardModuleItemState
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardParentProgramState
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardState
import com.instructure.horizon.horizonui.molecules.StatusChipColor

internal suspend fun List<GetCoursesQuery.Enrollment>.mapToDashboardCourseCardState(
    context: Context,
    programs: List<Program>,
    nextModuleForCourse: suspend (Long?) -> DashboardCourseCardModuleItemState?
): List<DashboardCourseCardState> {
    val completed = this.filter { it.isCompleted() }.map { it.mapCompleted(context, programs) }
    val active = this.filter { it.isActive() }.map { it.mapActive(programs, nextModuleForCourse) }
    return (active + completed).sortedByDescending { it.lastAccessed }
}

internal fun List<Program>.mapToDashboardCourseCardState(context: Context,): List<DashboardCourseCardState> {
    return this.map { program ->
        DashboardCourseCardState(
            chipState = DashboardCourseCardChipState(
                label = context.getString(R.string.dashboardCourseCardProgramChipLabel),
                color = StatusChipColor.Grey
            ),
            description = context.getString(
                R.string.dashboardCourseCardProgramDetailsMessage,
                program.name
            ),
            buttonState = DashboardCourseCardButtonState(
                label = context.getString(R.string.dashboardNotStartedProgramDetailsLabel),
                onClickAction = CardClickAction.NavigateToProgram(program.id),
            ),
        )
    }
}

private fun GetCoursesQuery.Enrollment.isCompleted(): Boolean {
    return this.state == EnrollmentWorkflowState.completed
}

private fun GetCoursesQuery.Enrollment.isActive(): Boolean {
    return this.state == EnrollmentWorkflowState.active
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
        imageState = null,
        title = this.course?.name.orEmpty(),
        description = context.getString(R.string.dashboardCompletedCourseDetails),
        progress = 1.0,
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