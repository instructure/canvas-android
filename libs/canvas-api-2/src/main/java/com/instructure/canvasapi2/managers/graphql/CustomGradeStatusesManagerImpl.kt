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
 */
package com.instructure.canvasapi2.managers.graphql

import com.apollographql.apollo.ApolloClient
import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.enqueueQuery

class CustomGradeStatusesManagerImpl(private val apolloClient: ApolloClient) : CustomGradeStatusesManager {

    override suspend fun getCustomGradeStatuses(courseId: Long, forceNetwork: Boolean): CustomGradeStatusesQuery.Data? {
        val query = CustomGradeStatusesQuery(courseId.toString())
        val result = apolloClient.enqueueQuery(query, forceNetwork)
        return result.data
    }
}
