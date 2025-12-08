//
// Copyright (C) 2025-present Instructure, Inc.
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
import com.instructure.dataseedingapi.DeleteCustomGradeStatusMutation
import com.instructure.dataseedingapi.UpsertCustomGradeStatusMutation
import kotlinx.coroutines.runBlocking

object CustomStatusApi {

    fun upsertCustomGradeStatus(
        token: String,
        name: String,
        color: String,
        id: String? = null
    ): String? {
        val apolloClient = CanvasNetworkAdapter.getApolloClient(token)

        val mutationCall = UpsertCustomGradeStatusMutation(
            id = Optional.presentIfNotNull(id),
            color = color,
            name = name
        )

        return runBlocking {
            val response = apolloClient.mutation(mutationCall).executeV3()
            response.data?.upsertCustomGradeStatus?.customGradeStatus?.id
        }
    }

    fun deleteCustomGradeStatus(token: String, id: String) {
        val apolloClient = CanvasNetworkAdapter.getApolloClient(token)

        val mutationCall = DeleteCustomGradeStatusMutation(id = id)

        runBlocking {
            val response = apolloClient.mutation(mutationCall).executeV3()

            if (response.hasErrors()) {
                val errorMessages = response.errors?.joinToString(", ") { it.message }
                throw IllegalStateException("Failed to delete custom grade status '$id': $errorMessages")
            }

            val errors = response.data?.deleteCustomGradeStatus?.errors
            if (!errors.isNullOrEmpty()) {
                val errorMessages = errors.joinToString(", ") { "${it.attribute}: ${it.message}" }
                throw IllegalStateException("Failed to delete custom grade status '$id': $errorMessages")
            }
        }
    }
}