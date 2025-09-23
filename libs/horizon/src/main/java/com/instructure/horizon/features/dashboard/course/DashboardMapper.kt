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

import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.managers.graphql.Program
import com.instructure.canvasapi2.type.EnrollmentWorkflowState
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.course.card.CardClickAction
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardButtonState
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardModuleItemState
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardParentProgramState
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardState

internal suspend fun List<GetCoursesQuery.Enrollment>.mapToDashboardCourseCardState(
    programs: List<Program>,
    nextModuleForCourse: suspend (Long?) -> DashboardCourseCardModuleItemState?,
    acceptInvite: suspend (courseId: Long, enrollmentId: Long) -> Unit
): List<DashboardCourseCardState> {
    val invitationStates = this.filter { it.isInvited() }.map { it.mapInvitation(acceptInvite) }
    val completed = this.filter { it.isCompleted() }.map { it.mapCompleted(programs) }
    val active = this.filter { it.isActive() }.map { it.mapActive(programs, nextModuleForCourse) }
    return (invitationStates + active + completed).sortedByDescending { it.lastAccessed }
}

internal fun List<Program>.mapToDashboardCourseCardState(): List<DashboardCourseCardState> {
    return this.map { program ->
        DashboardCourseCardState(
            title = program.name,
            description = "Welcome! View your program to enroll in your first course.",
            buttonState = DashboardCourseCardButtonState(
                label = "Program details",
                iconRes = R.drawable.arrow_forward,
                onClickAction = CardClickAction.NavigateToProgram(program.id),
            ),
        )
    }
}

private fun GetCoursesQuery.Enrollment.isInvited(): Boolean {
    return this.state == EnrollmentWorkflowState.invited
}

private fun GetCoursesQuery.Enrollment.isCompleted(): Boolean {
    return this.state == EnrollmentWorkflowState.completed
}

private fun GetCoursesQuery.Enrollment.isActive(): Boolean {
    return this.state == EnrollmentWorkflowState.active
}

private fun GetCoursesQuery.Enrollment.mapInvitation(acceptInvite: suspend (Long, Long) -> Unit): DashboardCourseCardState {
    return DashboardCourseCardState(
        parentPrograms = null,
        imageUrl = null,
        title = this.course?.name.orEmpty(),
        description = "You’ve been invited to join this course.",
        progress = null,
        moduleItem = null,
        buttonState = DashboardCourseCardButtonState(
            label = "Accept",
            iconRes = null,
            onClickAction = CardClickAction.Action { },
            action = {
                acceptInvite(this.course?.id?.toLongOrNull() ?: -1, this.id?.toLongOrNull() ?: -1L)
            }
        ),
        onClickAction = null,
        lastAccessed = this.lastActivityAt
    )
}

private fun GetCoursesQuery.Enrollment.mapCompleted(programs: List<Program>): DashboardCourseCardState {
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
        imageUrl = null,
        title = this.course?.name.orEmpty(),
        description = "Congrats! You’ve completed your course. View your progress and scores on the Learn page.",
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
        imageUrl = this.course?.image_download_url,
        title = this.course?.name.orEmpty(),
        description = null,
        progress = this.course?.usersConnection?.nodes?.firstOrNull()?.courseProgression?.requirements?.completionPercentage ?: 0.0,
        moduleItem = nextModuleForCourse(this.course?.id?.toLongOrNull()),
        buttonState = null,
        onClickAction = CardClickAction.NavigateToCourse(this.course?.id?.toLongOrNull() ?: -1L),
        lastAccessed = this.lastActivityAt
    )
}