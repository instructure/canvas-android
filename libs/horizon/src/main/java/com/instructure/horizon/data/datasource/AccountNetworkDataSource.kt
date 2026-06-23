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
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.apis.ExperienceAPI
import com.instructure.canvasapi2.apis.HelpLinksAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.HelpLink
import com.instructure.canvasapi2.models.User
import javax.inject.Inject

class AccountNetworkDataSource @Inject constructor(
    private val userApi: UserAPI.UsersInterface,
    private val experienceApi: ExperienceAPI,
    private val helpLinksApi: HelpLinksAPI.HelpLinksAPI,
) {
    suspend fun getSelf(forceRefresh: Boolean): User {
        return userApi.getSelf(RestParams(isForceReadFromNetwork = forceRefresh)).dataOrThrow
    }

    suspend fun getExperiences(forceRefresh: Boolean): List<String> {
        return experienceApi.getExperienceSummary(RestParams(isForceReadFromNetwork = forceRefresh))
            .dataOrNull?.availableApps ?: emptyList()
    }

    suspend fun getHelpLinks(forceRefresh: Boolean): List<HelpLink> {
        // Not an official, documented endpoint, we just use the same endpoint the web implementation uses.
        return helpLinksApi.getCanvasHelpLinks(
            RestParams(apiVersion = "", isForceReadFromNetwork = forceRefresh)
        ).dataOrNull.orEmpty()
    }
}
