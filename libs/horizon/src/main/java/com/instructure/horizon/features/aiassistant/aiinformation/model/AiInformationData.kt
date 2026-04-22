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
package com.instructure.horizon.features.aiassistant.aiinformation.model

data class AiInformationData(
    val title: String,
    val permissionLevelText: String,
    val permissionLevel: String,
    val description: String,
    val permissionLevelsModalTriggerText: String,
    val modelNameText: String,
    val modelName: String,
    val nutritionFactsModalTriggerText: String,
    val permissionLevelsData: AiInformationPermissionLevelsData,
    val nutritionFactsData: AiInformationNutritionFactsData,
)

data class AiInformationPermissionLevelsData(
    val title: String,
    val currentFeatureText: String,
    val currentFeature: String,
    val closeButtonText: String,
    val levels: List<DataPermissionLevel>,
)

data class DataPermissionLevel(
    val level: String,
    val title: String,
    val description: String,
    val isHighlighted: Boolean = false,
)

data class AiInformationNutritionFactsData(
    val title: String,
    val featureName: String,
    val closeButtonText: String,
    val blocks: List<NutritionFactBlock>,
)

data class NutritionFactBlock(
    val blockTitle: String,
    val segments: List<NutritionFactSegment>,
)

data class NutritionFactSegment(
    val segmentTitle: String,
    val description: String,
    val value: String,
    val valueDescription: String? = null,
)
