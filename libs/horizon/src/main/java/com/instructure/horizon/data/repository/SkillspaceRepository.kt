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

import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.data.datasource.SkillspaceNetworkDataSource
import javax.inject.Inject

class SkillspaceRepository @Inject constructor(
    private val networkDataSource: SkillspaceNetworkDataSource,
    private val apiPrefs: ApiPrefs,
) {
    suspend fun getAuthenticatedSession(): AuthenticatedSession? {
        return networkDataSource.getAuthenticatedSession()
    }

    fun getEmbeddedSkillspaceUrl(): String {
        val baseUrl = apiPrefs.fullDomain
        val skillspaceUrl = "$baseUrl/career/skillspace"
        return "$skillspaceUrl?embedded=true"
    }
}
