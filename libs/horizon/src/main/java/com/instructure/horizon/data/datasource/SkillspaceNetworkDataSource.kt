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

import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.utils.ApiPrefs
import javax.inject.Inject

class SkillspaceNetworkDataSource @Inject constructor(
    private val oAuthApi: OAuthAPI.OAuthInterface,
    private val apiPrefs: ApiPrefs,
) {
    suspend fun getAuthenticatedSession(): AuthenticatedSession? {
        return oAuthApi.getAuthenticatedSession(
            apiPrefs.fullDomain,
            RestParams(isForceReadFromNetwork = true)
        ).dataOrNull
    }
}
