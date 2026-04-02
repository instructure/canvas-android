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
package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName

data class RubricCriterionRatingApiModel(
    val description: String,
    val points: Double,
    @SerializedName("long_description")
    val longDescription: String? = null
)

data class RubricCriterionApiModel(
    val description: String,
    val points: Double,
    val ratings: Map<String, RubricCriterionRatingApiModel>,
    @SerializedName("long_description")
    val longDescription: String? = null
)

data class CreateRubricModel(
    val title: String,
    val criteria: Map<String, RubricCriterionApiModel>
)

data class RubricAssociationModel(
    @SerializedName("association_id")
    val associationId: Long,
    @SerializedName("association_type")
    val associationType: String = "Assignment",
    val purpose: String = "grading"
)

data class CreateRubricWrapper(
    val rubric: CreateRubricModel,
    @SerializedName("rubric_association")
    val rubricAssociation: RubricAssociationModel
)

data class RubricRatingResponseModel(
    val id: String,
    val description: String,
    val points: Double
)

data class RubricCriterionResponseModel(
    val id: String,
    val description: String,
    val ratings: List<RubricRatingResponseModel> = emptyList()
)

data class RubricApiModel(
    val id: Long,
    val title: String? = null,
    @SerializedName("rubric_id")
    val rubricId: Long? = null,
    @SerializedName("data")
    val criteria: List<RubricCriterionResponseModel> = emptyList()
)