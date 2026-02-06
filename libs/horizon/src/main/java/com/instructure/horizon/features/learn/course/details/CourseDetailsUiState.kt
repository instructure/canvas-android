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
package com.instructure.horizon.features.learn.course.details

import androidx.annotation.StringRes
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.platform.LoadingState

data class CourseDetailsUiState(
    val loadingState: LoadingState = LoadingState(),
    val courseName: String = "",
    val courseProgress: Double = 0.0,
    val courseId: Long = -1L,
    val courseSyllabus: String = "",
    val parentPrograms: List<Program> = emptyList(),
    val availableTabs: List<CourseDetailsTab> = CourseDetailsTab.entries,
)

enum class CourseDetailsTab(@StringRes val titleRes: Int) {
    Overview(titleRes = R.string.overview),
    MyProgress(titleRes = R.string.myProgress),
    Scores(titleRes = R.string.scores),
    Notes(titleRes = R.string.notes),
    Tools(titleRes = R.string.tools),
}