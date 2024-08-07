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

package com.instructure.parentapp.features.dashboard

import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.utils.ColorKeeper


data class DashboardViewData(
    val userViewData: UserViewData? = null,
    val studentSelectorExpanded: Boolean = false,
    val studentItems: List<StudentItemViewModel> = emptyList(),
    val selectedStudent: User? = null,
    val unreadCount: Int = 0,
    val alertCount: Int = 0,
) {
    fun studentItems(): List<ItemViewModel> {
        return studentItems + AddStudentItemViewModel(ColorKeeper.getOrGenerateColor(selectedStudent).backgroundColor()) {}
    }
}

data class StudentItemViewData(
    val studentId: Long,
    val studentName: String,
    val avatarUrl: String
)

data class UserViewData(
    val name: String?,
    val pronouns: String?,
    val shortName: String?,
    val avatarUrl: String?,
    val email: String?
)
