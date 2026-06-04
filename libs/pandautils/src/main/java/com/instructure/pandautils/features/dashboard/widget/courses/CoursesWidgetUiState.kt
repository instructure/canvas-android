/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.courses

import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.features.dashboard.widget.courses.model.CourseCardItem
import com.instructure.pandautils.features.dashboard.widget.courses.model.GroupCardItem
import com.instructure.pandautils.utils.ThemedColor

data class CoursesWidgetUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val courses: List<CourseCardItem> = emptyList(),
    val groups: List<GroupCardItem> = emptyList(),
    val isCoursesExpanded: Boolean = true,
    val isGroupsExpanded: Boolean = true,
    val showGrades: Boolean = false,
    val showColorOverlay: Boolean = false,
    val color: ThemedColor = ThemedColor(GlobalConfig.DEFAULT_COLOR),
    val onCourseClick: (Long) -> Unit = {},
    val onGroupClick: (Long) -> Unit = {},
    val onToggleCoursesExpanded: () -> Unit = {},
    val onToggleGroupsExpanded: () -> Unit = {},
    val onManageOfflineContent: (Long) -> Unit = {},
    val onCustomizeCourse: (Long) -> Unit = {},
    val onAllCourses: () -> Unit = {},
    val onAnnouncementClick: (Long) -> Unit = {},
    val onGroupMessageClick: (Long) -> Unit = {},
    val onCourseMoved: (Int, Int) -> Unit = { _, _ -> },
)