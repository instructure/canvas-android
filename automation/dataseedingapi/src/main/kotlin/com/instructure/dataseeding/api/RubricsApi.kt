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
 *
 */
package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.CreateRubricModel
import com.instructure.dataseeding.model.CreateRubricWrapper
import com.instructure.dataseeding.model.RubricApiModel
import com.instructure.dataseeding.model.RubricAssociationModel
import com.instructure.dataseeding.model.RubricCriterionApiModel
import com.instructure.dataseeding.model.RubricCriterionRatingApiModel
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

object RubricsApi {

    interface RubricsService {
        @POST("courses/{courseId}/rubrics")
        fun createRubric(
            @Path("courseId") courseId: Long,
            @Body createRubricWrapper: CreateRubricWrapper
        ): Call<RubricApiModel>
    }

    private fun rubricsService(token: String): RubricsService =
        CanvasNetworkAdapter.retrofitWithToken(token).create(RubricsService::class.java)

    data class RatingRequest(
        val description: String,
        val points: Double,
        val longDescription: String? = null
    )

    data class RubricCriterionRequest(
        val description: String,
        val points: Double,
        val ratings: List<RatingRequest>,
        val longDescription: String? = null
    )

    fun createRubricWithAssignment(
        courseId: Long,
        assignmentId: Long,
        teacherToken: String,
        title: String = "Test Rubric",
        criteria: List<RubricCriterionRequest>
    ): RubricApiModel {
        val criteriaMap = criteria.mapIndexed { index, criterion ->
            index.toString() to RubricCriterionApiModel(
                description = criterion.description,
                points = criterion.points,
                longDescription = criterion.longDescription,
                ratings = criterion.ratings.mapIndexed { ratingIndex, rating ->
                    ratingIndex.toString() to RubricCriterionRatingApiModel(
                        description = rating.description,
                        points = rating.points,
                        longDescription = rating.longDescription
                    )
                }.toMap()
            )
        }.toMap()

        val wrapper = CreateRubricWrapper(
            rubric = CreateRubricModel(title = title, criteria = criteriaMap),
            rubricAssociation = RubricAssociationModel(associationId = assignmentId)
        )

        return rubricsService(teacherToken)
            .createRubric(courseId, wrapper)
            .execute()
            .body()!!
    }
}