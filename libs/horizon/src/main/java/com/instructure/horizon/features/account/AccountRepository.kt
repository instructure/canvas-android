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
package com.instructure.horizon.features.account

import com.instructure.canvasapi2.apis.ExperienceAPI
import com.instructure.canvasapi2.apis.HelpLinksAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.HelpLink
import com.instructure.canvasapi2.models.User
import com.instructure.horizon.data.datasource.AccountLocalDataSource
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val userApi: UserAPI.UsersInterface,
    private val experienceApi: ExperienceAPI,
    private val helpLinksApi: HelpLinksAPI.HelpLinksAPI,
    private val localDataSource: AccountLocalDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getUserDetails(forceRefresh: Boolean): User {
        return if (shouldFetchFromNetwork()) {
            val restParams = RestParams(isForceReadFromNetwork = forceRefresh)
            val user = userApi.getSelf(restParams).dataOrThrow
            if (shouldSync()) localDataSource.saveUser(user)
            user
        } else {
            localDataSource.getUser() ?: User()
        }
    }

    suspend fun getExperiences(forceRefresh: Boolean): List<String> {
        if (!shouldFetchFromNetwork()) return emptyList()
        val restParams = RestParams(isForceReadFromNetwork = forceRefresh)
        return experienceApi.getExperienceSummary(restParams).dataOrNull?.availableApps ?: emptyList()
    }

    suspend fun getHelpLinks(forceRefresh: Boolean): List<HelpLink> {
        if (!shouldFetchFromNetwork()) return emptyList()
        // This not an official, documented endpoint, we just use the same endpoint to the web implementation
        return helpLinksApi.getCanvasHelpLinks(
            RestParams(apiVersion = "", isForceReadFromNetwork = forceRefresh)
        ).dataOrNull.orEmpty()
    }
}