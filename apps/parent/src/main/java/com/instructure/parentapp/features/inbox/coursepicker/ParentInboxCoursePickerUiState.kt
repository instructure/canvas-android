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
package com.instructure.parentapp.features.inbox.coursepicker

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.utils.ScreenState

data class ParentInboxCoursePickerUiState(
    val screenState: ScreenState = ScreenState.Loading,
    val studentContextItems: List<StudentContextItem> = emptyList()
)

data class StudentContextItem(
    val course: Course,
    val user: User
)

sealed class ParentInboxCoursePickerAction {
    data class StudentContextSelected(val studentContextItem: StudentContextItem): ParentInboxCoursePickerAction()
    data object CloseDialog: ParentInboxCoursePickerAction()
}

sealed class ParentInboxCoursePickerBottomSheetAction {
    data class NavigateToCompose(val options: InboxComposeOptions): ParentInboxCoursePickerBottomSheetAction()
    data object Dismiss: ParentInboxCoursePickerBottomSheetAction()
}