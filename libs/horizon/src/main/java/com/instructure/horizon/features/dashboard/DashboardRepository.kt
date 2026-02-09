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
package com.instructure.horizon.features.dashboard

import com.instructure.canvasapi2.apis.ThemeAPI
import com.instructure.canvasapi2.apis.UnreadCountAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.UnreadNotificationCount
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val apiPrefs: ApiPrefs,
    private val themeApi: ThemeAPI.ThemeInterface,
    private val userApi: UserAPI.UsersInterface,
    private val unreadCountApi: UnreadCountAPI.UnreadCountsInterface,
) {
    suspend fun getUnreadCounts(forceNetwork: Boolean): List<UnreadNotificationCount> {
        return unreadCountApi.getNotificationsCount(RestParams(isForceReadFromNetwork = forceNetwork)).dataOrNull.orEmpty()
    }

    suspend fun getTheme(): CanvasTheme? {
        val params = RestParams(isForceReadFromNetwork = false)
        return themeApi.getTheme(params).dataOrNull
    }

    suspend fun getSelf(): User? {
        val params = RestParams(isForceReadFromNetwork = true)
        return userApi.getSelf(params).dataOrNull
    }
}