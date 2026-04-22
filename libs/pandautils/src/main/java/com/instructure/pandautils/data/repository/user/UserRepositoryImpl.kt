/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.pandautils.data.repository.user

import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Account
import com.instructure.canvasapi2.models.BecomeUserPermission
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.ColorChangeResponse
import com.instructure.canvasapi2.models.DashboardPositions
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.ColorUtils.toApiHexString

class UserRepositoryImpl(
    private val userApi: UserAPI.UsersInterface
) : UserRepository {

    override suspend fun getSelf(forceRefresh: Boolean): DataResult<User> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return userApi.getSelf(params)
    }

    override suspend fun getSelfWithUuid(forceRefresh: Boolean): DataResult<User> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return userApi.getSelfWithUUID(params)
    }

    override suspend fun getColors(forceRefresh: Boolean): DataResult<CanvasColor> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return userApi.getColors(params)
    }

    override suspend fun getBecomeUserPermission(forceRefresh: Boolean): DataResult<BecomeUserPermission> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return userApi.getBecomeUserPermission(params)
    }

    override suspend fun getAccount(forceRefresh: Boolean): DataResult<Account> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return userApi.getAccount(params)
    }

    override suspend fun setCourseColor(contextId: String, color: Int): DataResult<ColorChangeResponse> {
        val params = RestParams(isForceReadFromNetwork = true)
        return userApi.setColor(contextId, color.toApiHexString(), params)
    }

    override suspend fun updateDashboardPositions(positions: DashboardPositions): DataResult<DashboardPositions> {
        val params = RestParams(isForceReadFromNetwork = true)
        val result = userApi.updateDashboardPositions(positions, params)
        if (result is DataResult.Success) {
            CanvasRestAdapter.clearCacheUrls("dashboard/dashboard_cards")
        }
        return result
    }
}
