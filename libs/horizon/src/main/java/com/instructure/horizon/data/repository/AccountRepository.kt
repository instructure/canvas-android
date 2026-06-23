/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.data.repository

import com.instructure.canvasapi2.models.HelpLink
import com.instructure.canvasapi2.models.User
import com.instructure.horizon.data.datasource.AccountLocalDataSource
import com.instructure.horizon.data.datasource.AccountNetworkDataSource
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val networkDataSource: AccountNetworkDataSource,
    private val localDataSource: AccountLocalDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getUserDetails(forceRefresh: Boolean): User {
        return if (shouldFetchFromNetwork()) {
            val user = networkDataSource.getSelf(forceRefresh)
            if (shouldSync()) localDataSource.saveUser(user)
            user
        } else {
            localDataSource.getUser() ?: User()
        }
    }

    suspend fun getExperiences(forceRefresh: Boolean): List<String> {
        if (!shouldFetchFromNetwork()) return emptyList()
        return networkDataSource.getExperiences(forceRefresh)
    }

    suspend fun getHelpLinks(forceRefresh: Boolean): List<HelpLink> {
        if (!shouldFetchFromNetwork()) return emptyList()
        return networkDataSource.getHelpLinks(forceRefresh)
    }
}
