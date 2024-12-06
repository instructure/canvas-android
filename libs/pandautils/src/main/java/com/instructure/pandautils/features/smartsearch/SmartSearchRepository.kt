/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.smartsearch

import com.instructure.canvasapi2.apis.SmartSearchApi
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.SmartSearchFilter
import com.instructure.canvasapi2.models.SmartSearchResult

class SmartSearchRepository(private val smartSearchApi: SmartSearchApi) {

    suspend fun smartSearch(courseId: Long, query: String, filter: List<SmartSearchFilter> = emptyList()): List<SmartSearchResult> {
        return smartSearchApi.smartSearch(
            courseId,
            query,
            filter.map { it.name.lowercase() },
            RestParams(isForceReadFromNetwork = true)
        ).dataOrThrow
            .results
            .filter { it.relevance >= 50 }
    }
}