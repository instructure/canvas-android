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

import com.instructure.pandautils.mvvm.ItemViewModel

data class GradesViewData(val items: List<ItemViewModel>)

data class GradingPeriod(val id: Long, val name: String)

data class GradeRowViewData(val courseId: Long, val courseName: String, val courseColor: String, val score: Double?, val gradeText: String)

sealed class GradesAction

enum class GradesItemViewType(val viewType: Int) {
    GRADING_PERIOD_SELECTOR(0),
    GRADE_ROW(1)
}