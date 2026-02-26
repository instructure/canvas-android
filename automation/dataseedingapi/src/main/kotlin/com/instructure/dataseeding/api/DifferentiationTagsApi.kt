//
// Copyright (C) 2026-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.instructure.dataseeding.api

import com.apollographql.apollo.api.Optional
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseedingapi.CreateGroupInSetMutation
import com.instructure.dataseedingapi.CreateGroupSetMutation
import com.instructure.dataseedingapi.type.GroupSetContextType
import kotlinx.coroutines.runBlocking

object DifferentiationTagsApi {

    /**
     * Creates a group set (category) for differentiation tags.
     * Differentiation tags use non-collaborative group sets.
     *
     * @param token Authorization token
     * @param courseId The course ID to create the group set in
     * @param name Name of the group set
     * @param nonCollaborative Set to true for differentiation tags (default: true)
     * @return The ID of the created group set, or null if creation failed
     */
    fun createGroupSet(
        token: String,
        courseId: String,
        name: String,
        nonCollaborative: Boolean = true
    ): String {
        val apolloClient = CanvasNetworkAdapter.getApolloClient(token)

        val mutationCall = CreateGroupSetMutation(
            contextId = courseId,
            contextType = GroupSetContextType.course,
            name = name,
            nonCollaborative = Optional.presentIfNotNull(nonCollaborative)
        )

        return runBlocking {
            val response = apolloClient.mutation(mutationCall).executeV3()

            if (response.hasErrors()) {
                val errorMessages = response.errors?.joinToString(", ") { it.message }
                throw IllegalStateException("Failed to create group set '$name': $errorMessages")
            }

            val errors = response.data?.createGroupSet?.errors
            if (!errors.isNullOrEmpty()) {
                val errorMessages = errors.joinToString(", ") { "${it.attribute}: ${it.message}" }
                throw IllegalStateException("Failed to create group set '$name': $errorMessages")
            }

            response.data?.createGroupSet?.groupSet?._id!!
        }
    }

    /**
     * Creates a group within a group set.
     * For differentiation tags, this creates a tag within the set.
     *
     * @param token Authorization token
     * @param groupSetId The group set ID to create the group in
     * @param name Name of the group (differentiation tag)
     * @param nonCollaborative Set to true for differentiation tags (default: true)
     * @return The ID of the created group, or null if creation failed
     */
    fun createGroup(
        token: String,
        groupSetId: String,
        name: String,
        nonCollaborative: Boolean = true
    ): String {
        val apolloClient = CanvasNetworkAdapter.getApolloClient(token)

        val mutationCall = CreateGroupInSetMutation(
            groupSetId = groupSetId,
            name = name,
            nonCollaborative = Optional.presentIfNotNull(nonCollaborative)
        )

        return runBlocking {
            val response = apolloClient.mutation(mutationCall).executeV3()

            if (response.hasErrors()) {
                val errorMessages = response.errors?.joinToString(", ") { it.message }
                throw IllegalStateException("Failed to create group '$name': $errorMessages")
            }

            val errors = response.data?.createGroupInSet?.errors
            if (!errors.isNullOrEmpty()) {
                val errorMessages = errors.joinToString(", ") { "${it.attribute}: ${it.message}" }
                throw IllegalStateException("Failed to create group '$name': $errorMessages")
            }

            response.data?.createGroupInSet?.group?._id!!
        }
    }

    /**
     * Adds a user to a group using the REST API.
     * For differentiation tags, this assigns a student to a tag.
     * Note: Canvas GraphQL doesn't support adding users to groups, so we use the REST API from GroupsApi.
     *
     * @param token Authorization token
     * @param groupId The group ID to add the user to
     * @param userId The user ID to add to the group
     * @return The group membership model
     */
    fun addUserToGroup(
        token: String,
        groupId: Long,
        userId: Long
    ) = GroupsApi.createGroupMembership(groupId, userId, token)
}