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

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.LaunchDefinitionsAPI
import com.instructure.canvasapi2.apis.UnreadCountAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.utils.orDefault


class DashboardRepository(
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface,
    private val unreadCountApi: UnreadCountAPI.UnreadCountsInterface,
    private val launchDefinitionsApi: LaunchDefinitionsAPI.LaunchDefinitionsInterface
) {

    suspend fun getStudents(forceNetwork: Boolean): List<User> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        return enrollmentApi.firstPageObserveeEnrollmentsParent(params).depaginate {
            enrollmentApi.getNextPage(it, params)
        }.dataOrNull
            .orEmpty()
            .mapNotNull { it.observedUser }
            .distinct()
            .sortedBy { it.sortableName }
    }

    suspend fun getUnreadCounts(): Int {
        val params = RestParams(isForceReadFromNetwork = true)
        val unreadCount = unreadCountApi.getUnreadConversationCount(params).dataOrNull?.unreadCount ?: "0"
        return unreadCount.toIntOrNull().orDefault()
    }

    suspend fun getLaunchDefinitions(): List<LaunchDefinition> {
        val params = RestParams(isForceReadFromNetwork = false)
        return launchDefinitionsApi.getLaunchDefinitions(params).dataOrNull.orEmpty()
    }
}
