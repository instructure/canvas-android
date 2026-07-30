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

import com.instructure.canvasapi2.managers.graphql.horizon.journey.Skill
import com.instructure.horizon.data.datasource.SkillsNetworkDataSource
import javax.inject.Inject

class SkillsRepository @Inject constructor(
    private val networkDataSource: SkillsNetworkDataSource,
) {
    suspend fun getSkills(completedOnly: Boolean?, forceNetwork: Boolean): List<Skill> {
        return networkDataSource.getSkills(completedOnly, forceNetwork)
    }
}
