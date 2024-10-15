/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.courses.details

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.instructure.parentapp.R


data class CourseDetailsUiState(
    val courseName: String = "",
    @ColorInt val studentColor: Int = Color.BLACK,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val tabs: List<TabType> = emptyList(),
    val currentTab: TabType? = null
)

enum class TabType(@StringRes val labelRes: Int) {
    GRADES(R.string.courseGradesLabel),
    FRONT_PAGE(R.string.courseFrontPageLabel),
    SYLLABUS(R.string.courseSyllabusLabel),
    SUMMARY(R.string.courseSummaryLabel)
}

sealed class CourseDetailsAction {
    data object Refresh : CourseDetailsAction()
    data object SendAMessage : CourseDetailsAction()
    data class NavigateToAssignmentDetails(val id: Long) : CourseDetailsAction()
    data class CurrentTabChanged(val newTab: TabType) : CourseDetailsAction()
}

sealed class CourseDetailsViewModelAction {
    data object NavigateToComposeMessageScreen : CourseDetailsViewModelAction()
    data class NavigateToAssignmentDetails(val id: Long) : CourseDetailsViewModelAction()
}
