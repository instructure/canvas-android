/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.mycontent.common

import android.content.res.Resources
import com.instructure.canvasapi2.models.journey.mycontent.CourseEnrollmentItem
import com.instructure.canvasapi2.models.journey.mycontent.LearnItem
import com.instructure.canvasapi2.models.journey.mycontent.ProgramEnrollmentItem
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.pandautils.utils.formatMonthDayYear
import com.instructure.pandautils.utils.orDefault

suspend fun LearnItem.toCardState(
    resources: Resources,
    fetchNextModuleItemRoute: suspend (courseId: Long?) -> Any?
): LearnContentCardState {
    return when (this) {
        is ProgramEnrollmentItem -> toCardState(resources)
        is CourseEnrollmentItem -> toCardState(resources, fetchNextModuleItemRoute)
    }
}

private fun ProgramEnrollmentItem.toCardState(resources: Resources): LearnContentCardState {
    return LearnContentCardState(
        imageUrl = null,
        name = name,
        progress = completionPercentage,
        route = LearnRoute.LearnProgramDetailsScreen.route(id),
        buttonState = null,
        cardChips = buildList {
            add(
                LearnContentCardChipState(
                    label = resources.getString(R.string.learnMyContentProgramLabel),
                    color = StatusChipColor.Violet,
                    iconRes = R.drawable.book_5,
                )
            )
            add(
                LearnContentCardChipState(
                    label = resources.getQuantityString(
                        R.plurals.learnMyContentProgramCourseCount,
                        courseCount,
                        courseCount,
                    ),
                )
            )
            val estimatedDurationMinutes = estimatedDurationMinutes
            if (estimatedDurationMinutes != null && estimatedDurationMinutes > 0) {
                val hours = estimatedDurationMinutes / 60
                val mins = estimatedDurationMinutes % 60
                val label = when {
                    hours > 0 && mins > 0 -> resources.getString(R.string.learnMyContentDurationHrsMin, hours, mins)
                    hours > 0 -> resources.getString(R.string.learnMyContentDurationHrs, hours)
                    else -> resources.getString(R.string.learnMyContentDurationMin, mins)
                }
                add(LearnContentCardChipState(label = label))
            }
            if (startDate != null && endDate != null) {
                add(
                    LearnContentCardChipState(
                        label = resources.getString(
                            R.string.programTag_DateRange,
                            startDate?.formatMonthDayYear(),
                            endDate?.formatMonthDayYear(),
                        ),
                        iconRes = R.drawable.calendar_today,
                    )
                )
            }
        },
    )
}

private suspend fun CourseEnrollmentItem.toCardState(
    resources: Resources,
    fetchNextModuleItemRoute: suspend (courseId: Long?) -> Any?
): LearnContentCardState {
    val buttonLabel = when {
        completionPercentage == null || completionPercentage == 0.0 -> resources.getString(R.string.learnMyContentStartLearning)
        completionPercentage.orDefault() < 100.0 -> resources.getString(R.string.learnMyContentResumeLearning)
        else -> null
    }
    val buttonRoute = fetchNextModuleItemRoute(this.id.toLongOrNull())
    val buttonState = if (buttonLabel != null && buttonRoute != null) {
        LearnContentCardButtonState(buttonLabel, buttonRoute)
    } else { null }

    return LearnContentCardState(
        imageUrl = imageUrl,
        name = name,
        progress = completionPercentage,
        route = LearnRoute.LearnCourseDetailsScreen.route(id.toLongOrNull() ?: -1L),
        buttonState = buttonState,
        cardChips = buildList {
            add(
                LearnContentCardChipState(
                    label = resources.getString(R.string.learnMyContentCourseLabel),
                    color = StatusChipColor.Institution,
                    iconRes = R.drawable.book_2,
                )
            )
            if (startAt != null && endAt != null) {
                add(
                    LearnContentCardChipState(
                        label = resources.getString(
                            R.string.programTag_DateRange,
                            startAt?.formatMonthDayYear(),
                            endAt?.formatMonthDayYear(),
                        ),
                        iconRes = R.drawable.calendar_today,
                    )
                )
            }
        },
    )
}
