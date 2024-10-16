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

import android.net.Uri
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.mvvm.ItemViewModel


data class DashboardViewData(
    val userViewData: UserViewData? = null,
    val studentSelectorExpanded: Boolean = false,
    val studentItems: List<ItemViewModel> = emptyList(),
    val selectedStudent: User? = null,
    val unreadCount: Int = 0,
    val alertCount: Int = 0,
    val launchDefinitionViewData: List<LaunchDefinitionViewData> = emptyList(),
)

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

data class LaunchDefinitionViewData(
    val name: String,
    val domain: String,
    val url: String
)

sealed class DashboardViewModelAction {
    data object AddStudent : DashboardViewModelAction()
    data class NavigateDeepLink(val deepLinkUri: Uri) : DashboardViewModelAction()
    data class OpenLtiTool(val url: String, val name: String) : DashboardViewModelAction()
}

enum class StudentListViewType(val viewType: Int) {
    STUDENT(0),
    ADD_STUDENT(1)
}