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
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.ModuleItemCheckpointsQuery
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.enqueueQuery

class ModuleManagerImpl(private val apolloClient: ApolloClient) : ModuleManager {
    override suspend fun getModuleItemCheckpoints(courseId: String, forceNetwork: Boolean): List<ModuleItemWithCheckpoints> {
        var hasNextPage = true
        var nextCursor: String? = null
        val moduleItemsWithCheckpoints = mutableListOf<ModuleItemWithCheckpoints>()

        while (hasNextPage) {
            val nextCursorParam = if (nextCursor != null) Optional.present(nextCursor) else Optional.absent()
            val query = ModuleItemCheckpointsQuery(courseId, QLClientConfig.GRAPHQL_PAGE_SIZE, nextCursorParam)
            val data = apolloClient.enqueueQuery(query, forceNetwork).data
            val modulesConnection = data?.course?.modulesConnection

            val newItems = modulesConnection?.edges
                ?.flatMap { edge ->
                    edge?.node?.moduleItems?.mapNotNull { moduleItem ->
                        val discussion = moduleItem.content?.onDiscussion
                        if (discussion != null && !discussion.checkpoints.isNullOrEmpty()) {
                            val checkpoints = discussion.checkpoints.map { checkpoint ->
                                ModuleItemCheckpoint(
                                    dueAt = checkpoint.dueAt,
                                    tag = checkpoint.tag,
                                    pointsPossible = checkpoint.pointsPossible
                                )
                            }
                            ModuleItemWithCheckpoints(
                                moduleItemId = moduleItem._id,
                                checkpoints = checkpoints
                            )
                        } else {
                            null
                        }
                    } ?: emptyList()
                } ?: emptyList()

            moduleItemsWithCheckpoints.addAll(newItems)
            hasNextPage = modulesConnection?.pageInfo?.hasNextPage ?: false
            nextCursor = modulesConnection?.pageInfo?.endCursor
        }

        return moduleItemsWithCheckpoints
    }
}