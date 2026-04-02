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
import com.instructure.canvasapi2.managers.graphql.horizon.DashboardEnrollment
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
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

internal suspend fun List<DashboardEnrollment>.mapToDashboardCourseCardState(
    context: Context,
    programs: List<Program>,
    nextModuleForCourse: suspend (Long) -> DashboardCourseCardModuleItemState?
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

private fun DashboardEnrollment.isCompleted(): Boolean {
    return enrollmentState == DashboardEnrollment.STATE_COMPLETED || completionPercentage == 100.0
}

private fun DashboardEnrollment.isActive(): Boolean {
    return enrollmentState == DashboardEnrollment.STATE_ACTIVE && completionPercentage != 100.0
}

private fun List<DashboardEnrollment>.mapCompleted(context: Context, programs: List<Program>): List<DashboardCourseCardState> {
    return map { enrollment ->
        DashboardCourseCardState(
            parentPrograms = programs
                .filter { it.sortedRequirements.any { req -> req.courseId == enrollment.courseId } }
                .map { program ->
                    DashboardCourseCardParentProgramState(
                        programName = program.name,
                        programId = program.id,
                        onClickAction = CardClickAction.NavigateToProgram(program.id)
                    )
                },
            imageState = DashboardCourseCardImageState(
                imageUrl = enrollment.courseImageUrl,
                showPlaceholder = true
            ),
            title = enrollment.courseName,
            descriptionState = DashboardCourseCardDescriptionState(
                descriptionTitle = context.getString(R.string.dashboardCompletedCourseTitle),
                description = context.getString(R.string.dashboardCompletedCourseMessage),
            ),
            progress = enrollment.completionPercentage,
            moduleItem = null,
            onClickAction = CardClickAction.NavigateToCourse(enrollment.courseId)
        )
    }
}

private suspend fun List<DashboardEnrollment>.mapActive(
    programs: List<Program>,
    nextModuleForCourse: suspend (Long) -> DashboardCourseCardModuleItemState?
): List<DashboardCourseCardState> {
    return map { enrollment ->
        DashboardCourseCardState(
            parentPrograms = programs
                .filter { it.sortedRequirements.any { req -> req.courseId == enrollment.courseId } }
                .map { program ->
                    DashboardCourseCardParentProgramState(
                        programName = program.name,
                        programId = program.id,
                        onClickAction = CardClickAction.NavigateToProgram(program.id)
                    )
                },
            imageState = DashboardCourseCardImageState(
                imageUrl = enrollment.courseImageUrl,
                showPlaceholder = true
            ),
            title = enrollment.courseName,
            descriptionState = null,
            progress = enrollment.completionPercentage,
            moduleItem = nextModuleForCourse(enrollment.courseId),
            onClickAction = CardClickAction.NavigateToCourse(enrollment.courseId),
        )
    }
}

private fun List<DashboardCourseCardState>.adjustAndSortCourseCardValues(): List<DashboardCourseCardState> {
    return sortedByDescending { course ->
        course.progress.run { if (this == 100.0) -1.0 else this }
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
