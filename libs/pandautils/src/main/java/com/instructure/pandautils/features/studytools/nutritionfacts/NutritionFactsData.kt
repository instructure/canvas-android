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
package com.instructure.pandautils.features.studytools.nutritionfacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.instructure.pandautils.R

data class NutritionFactsData(
    val title: String,
    val featureName: String,
    val blocks: List<NutritionFactBlock>
)

data class NutritionFactBlock(
    val blockTitle: String,
    val segments: List<NutritionFactSegment>
)

data class NutritionFactSegment(
    val segmentTitle: String,
    val description: String,
    val value: String
)

/**
 * The AI disclosure shown behind the header's help icon. The content mirrors Horizon's disclosure,
 * with the base model corrected to the one canvas-lms actually pins for Study Assist.
 */
@Composable
fun studyToolsNutritionFacts(): NutritionFactsData = NutritionFactsData(
    title = stringResource(R.string.studyTools_nutritionFacts_title),
    featureName = stringResource(R.string.studyTools_nutritionFacts_featureName),
    blocks = listOf(
        NutritionFactBlock(
            blockTitle = stringResource(R.string.studyTools_nutritionFacts_block_modelData),
            segments = listOf(
                NutritionFactSegment(
                    segmentTitle = stringResource(R.string.studyTools_nutritionFacts_baseModel_title),
                    description = stringResource(R.string.studyTools_nutritionFacts_baseModel_description),
                    value = stringResource(R.string.studyTools_nutritionFacts_baseModel_value)
                ),
                NutritionFactSegment(
                    segmentTitle = stringResource(R.string.studyTools_nutritionFacts_trainedWithUserData_title),
                    description = stringResource(R.string.studyTools_nutritionFacts_trainedWithUserData_description),
                    value = stringResource(R.string.studyTools_nutritionFacts_trainedWithUserData_value)
                ),
                NutritionFactSegment(
                    segmentTitle = stringResource(R.string.studyTools_nutritionFacts_dataSharedWithModel_title),
                    description = stringResource(R.string.studyTools_nutritionFacts_dataSharedWithModel_description),
                    value = stringResource(R.string.studyTools_nutritionFacts_dataSharedWithModel_value)
                )
            )
        ),
        NutritionFactBlock(
            blockTitle = stringResource(R.string.studyTools_nutritionFacts_block_privacyCompliance),
            segments = listOf(
                NutritionFactSegment(
                    segmentTitle = stringResource(R.string.studyTools_nutritionFacts_dataRetention_title),
                    description = stringResource(R.string.studyTools_nutritionFacts_dataRetention_description),
                    value = stringResource(R.string.studyTools_nutritionFacts_dataRetention_value)
                ),
                NutritionFactSegment(
                    segmentTitle = stringResource(R.string.studyTools_nutritionFacts_dataLogging_title),
                    description = stringResource(R.string.studyTools_nutritionFacts_dataLogging_description),
                    value = stringResource(R.string.studyTools_nutritionFacts_dataLogging_value)
                ),
                NutritionFactSegment(
                    segmentTitle = stringResource(R.string.studyTools_nutritionFacts_regionsSupported_title),
                    description = stringResource(R.string.studyTools_nutritionFacts_regionsSupported_description),
                    value = stringResource(R.string.studyTools_nutritionFacts_regionsSupported_value)
                ),
                NutritionFactSegment(
                    segmentTitle = stringResource(R.string.studyTools_nutritionFacts_pii_title),
                    description = stringResource(R.string.studyTools_nutritionFacts_pii_description),
                    value = stringResource(R.string.studyTools_nutritionFacts_pii_value)
                )
            )
        ),
        NutritionFactBlock(
            blockTitle = stringResource(R.string.studyTools_nutritionFacts_block_outputs),
            segments = listOf(
                NutritionFactSegment(
                    segmentTitle = stringResource(R.string.studyTools_nutritionFacts_aiSettingsControl_title),
                    description = stringResource(R.string.studyTools_nutritionFacts_aiSettingsControl_description),
                    value = stringResource(R.string.studyTools_nutritionFacts_aiSettingsControl_value)
                ),
                NutritionFactSegment(
                    segmentTitle = stringResource(R.string.studyTools_nutritionFacts_humanInTheLoop_title),
                    description = stringResource(R.string.studyTools_nutritionFacts_humanInTheLoop_description),
                    value = stringResource(R.string.studyTools_nutritionFacts_humanInTheLoop_value)
                ),
                NutritionFactSegment(
                    segmentTitle = stringResource(R.string.studyTools_nutritionFacts_guardrails_title),
                    description = stringResource(R.string.studyTools_nutritionFacts_guardrails_description),
                    value = stringResource(R.string.studyTools_nutritionFacts_guardrails_value)
                ),
                NutritionFactSegment(
                    segmentTitle = stringResource(R.string.studyTools_nutritionFacts_expectedRisks_title),
                    description = stringResource(R.string.studyTools_nutritionFacts_expectedRisks_description),
                    value = stringResource(R.string.studyTools_nutritionFacts_expectedRisks_value)
                ),
                NutritionFactSegment(
                    segmentTitle = stringResource(R.string.studyTools_nutritionFacts_intendedOutcomes_title),
                    description = stringResource(R.string.studyTools_nutritionFacts_intendedOutcomes_description),
                    value = stringResource(R.string.studyTools_nutritionFacts_intendedOutcomes_value)
                )
            )
        )
    )
)
