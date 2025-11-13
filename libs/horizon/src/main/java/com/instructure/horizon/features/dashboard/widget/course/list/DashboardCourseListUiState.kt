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
package com.instructure.horizon.features.dashboard.widget.course.list

import android.content.Context
import androidx.annotation.StringRes
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.platform.LoadingState

data class DashboardCourseListUiState(
    val loadingState: LoadingState = LoadingState(),
    val courses: List<DashboardCourseListCourseState> = emptyList(),
    val filterOptions: List<DashboardCourseListFilterOption> = DashboardCourseListFilterOption.entries,
    val selectedFilterOption: DashboardCourseListFilterOption = DashboardCourseListFilterOption.All,
    val onFilterOptionSelected: (DashboardCourseListFilterOption) -> Unit = {},
    val onRefresh: () -> Unit = {},
)

data class DashboardCourseListCourseState(
    val parentPrograms: List<DashboardCourseListParentProgramState>,
    val name: String,
    val courseId: Long,
    val progress: Double,
)

data class DashboardCourseListParentProgramState(
    val programName: String,
    val programId: String,
)

enum class DashboardCourseListFilterOption(@param:StringRes val labelRes: Int) {
    All(R.string.dashboardCourseListAllCoursesFilterLabel),
    NotStarted(R.string.dashboardCourseListNotStartedFilterLabel),
    InProgress(R.string.dashboardCourseListInProgressFilterLabel),
    Completed(R.string.dashboardCourseListCompletedFilterLabel);

    companion object {
        fun fromLabelRes(@StringRes labelRes: Int): DashboardCourseListFilterOption {
            return entries.firstOrNull { it.labelRes == labelRes } ?: All
        }

        fun fromLabel(context: Context, label: String): DashboardCourseListFilterOption {
            return entries.firstOrNull { context.getString(it.labelRes) == label } ?: All
        }
    }
}