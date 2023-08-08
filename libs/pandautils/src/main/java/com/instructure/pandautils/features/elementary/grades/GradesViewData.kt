/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.grades

import androidx.annotation.ColorInt
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.utils.ThemedColor

data class GradesViewData(val items: List<ItemViewModel>)

data class GradingPeriod(val id: Long, val name: String)

data class GradeRowViewData(
    val courseId: Long,
    val courseName: String,
    val courseColor: ThemedColor,
    val courseImageUrl: String,
    val score: Double?,
    val gradeText: String,
    val hideProgress: Boolean = false)

sealed class GradesAction {
    data class OpenCourseGrades(val course: Course) : GradesAction()
    data class OpenGradingPeriodsDialog(val gradingPeriods: List<GradingPeriod>, val selectedGradingPeriodIndex: Int) : GradesAction()
    object ShowGradingPeriodError : GradesAction()
    object ShowRefreshError : GradesAction()
}

enum class GradesItemViewType(val viewType: Int) {
    GRADING_PERIOD_SELECTOR(0),
    GRADE_ROW(1)
}