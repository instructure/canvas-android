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
package com.instructure.horizon.features.aiassistant.aiinformation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.aiassistant.aiinformation.model.AiInformationData
import com.instructure.horizon.features.aiassistant.aiinformation.model.AiInformationNutritionFactsData
import com.instructure.horizon.features.aiassistant.aiinformation.model.AiInformationPermissionLevelsData
import com.instructure.horizon.features.aiassistant.aiinformation.model.DataPermissionLevel
import com.instructure.horizon.features.aiassistant.aiinformation.model.NutritionFactBlock
import com.instructure.horizon.features.aiassistant.aiinformation.model.NutritionFactSegment
import com.instructure.horizon.features.aiassistant.aiinformation.nutritionfacts.AiInformationNutritionFactsScreen
import com.instructure.horizon.features.aiassistant.aiinformation.permissionlevels.AiInformationPermissionLevelsScreen
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.TextLink
import com.instructure.horizon.horizonui.molecules.TextLinkColor
import com.instructure.horizon.horizonui.molecules.TextLinkSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiInformationScreen(
    onDismiss: () -> Unit,
    data: AiInformationData = defaultAiInformationData(),
) {
    var showPermissionLevels by rememberSaveable { mutableStateOf(false) }
    var showNutritionFacts by rememberSaveable { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        containerColor = HorizonColors.Surface.pageSecondary(),
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            AiInformationHeader(onDismiss = onDismiss)
            HorizonSpace(SpaceSize.SPACE_16)
            Text(
                text = data.title,
                style = HorizonTypography.h1,
                color = HorizonColors.Text.title(),
            )
            HorizonSpace(SpaceSize.SPACE_24)
            Text(
                text = data.permissionLevelText,
                style = HorizonTypography.labelLargeBold,
                color = HorizonColors.Text.title(),
            )
            HorizonSpace(SpaceSize.SPACE_8)
            Text(
                text = data.permissionLevel,
                style = HorizonTypography.tag,
                color = HorizonColors.PrimitivesViolet.violet40(),
            )
            HorizonSpace(SpaceSize.SPACE_8)
            Text(
                text = data.description,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
            )
            HorizonSpace(SpaceSize.SPACE_16)
            TextLink(
                text = data.permissionLevelsModalTriggerText,
                textLinkColor = TextLinkColor.Black,
                textLinkSize = TextLinkSize.NORMAL,
                onClick = { showPermissionLevels = true },
            )
            HorizonSpace(SpaceSize.SPACE_24)
            Text(
                text = data.modelNameText,
                style = HorizonTypography.labelLargeBold,
                color = HorizonColors.Text.title(),
            )
            HorizonSpace(SpaceSize.SPACE_8)
            Text(
                text = data.modelName,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
            )
            HorizonSpace(SpaceSize.SPACE_16)
            TextLink(
                text = data.nutritionFactsModalTriggerText,
                textLinkColor = TextLinkColor.Black,
                textLinkSize = TextLinkSize.NORMAL,
                onClick = { showNutritionFacts = true },
            )
            HorizonSpace(SpaceSize.SPACE_32)
        }
    }

    if (showPermissionLevels) {
        AiInformationPermissionLevelsScreen(
            data = data.permissionLevelsData,
            onDismiss = { showPermissionLevels = false },
        )
    }

    if (showNutritionFacts) {
        AiInformationNutritionFactsScreen(
            data = data.nutritionFactsData,
            onDismiss = { showNutritionFacts = false },
        )
    }
}

@Composable
private fun defaultAiInformationData() = AiInformationData(
    title = stringResource(R.string.aiInformation_title),
    permissionLevelText = stringResource(R.string.aiInformation_permissionLevelText),
    permissionLevel = stringResource(R.string.aiInformation_permissionLevel),
    description = stringResource(R.string.aiInformation_description),
    permissionLevelsModalTriggerText = stringResource(R.string.aiInformation_permissionLevelsModalTriggerText),
    modelNameText = stringResource(R.string.aiInformation_modelNameText),
    modelName = stringResource(R.string.aiInformation_modelName),
    nutritionFactsModalTriggerText = stringResource(R.string.aiInformation_nutritionFactsModalTriggerText),
    permissionLevelsData = AiInformationPermissionLevelsData(
        title = stringResource(R.string.aiInformation_permissionLevels_title),
        currentFeatureText = stringResource(R.string.aiInformation_permissionLevels_currentFeatureText),
        currentFeature = stringResource(R.string.aiInformation_permissionLevels_currentFeature),
        closeButtonText = stringResource(R.string.aiInformation_close),
        levels = listOf(
            DataPermissionLevel(
                level = stringResource(R.string.aiInformation_level1),
                title = stringResource(R.string.aiInformation_level1_title),
                description = stringResource(R.string.aiInformation_level1_description),
            ),
            DataPermissionLevel(
                level = stringResource(R.string.aiInformation_level2),
                title = stringResource(R.string.aiInformation_level2_title),
                description = stringResource(R.string.aiInformation_level2_description),
                isHighlighted = true,
            ),
            DataPermissionLevel(
                level = stringResource(R.string.aiInformation_level3),
                title = stringResource(R.string.aiInformation_level3_title),
                description = stringResource(R.string.aiInformation_level3_description),
            ),
            DataPermissionLevel(
                level = stringResource(R.string.aiInformation_level4),
                title = stringResource(R.string.aiInformation_level4_title),
                description = stringResource(R.string.aiInformation_level4_description),
            ),
        ),
    ),
    nutritionFactsData = AiInformationNutritionFactsData(
        title = stringResource(R.string.aiInformation_nutritionFacts_title),
        featureName = stringResource(R.string.aiInformation_nutritionFacts_featureName),
        closeButtonText = stringResource(R.string.aiInformation_close),
        blocks = listOf(
            NutritionFactBlock(
                blockTitle = stringResource(R.string.aiInformation_block_modelData),
                segments = listOf(
                    NutritionFactSegment(
                        segmentTitle = stringResource(R.string.aiInformation_segment_baseModel_title),
                        description = stringResource(R.string.aiInformation_segment_baseModel_description),
                        value = stringResource(R.string.aiInformation_modelName),
                    ),
                    NutritionFactSegment(
                        segmentTitle = stringResource(R.string.aiInformation_segment_trainedWithUserData_title),
                        description = stringResource(R.string.aiInformation_segment_trainedWithUserData_description),
                        value = stringResource(R.string.aiInformation_segment_trainedWithUserData_value),
                    ),
                    NutritionFactSegment(
                        segmentTitle = stringResource(R.string.aiInformation_segment_dataSharedWithModel_title),
                        description = stringResource(R.string.aiInformation_segment_dataSharedWithModel_description),
                        value = stringResource(R.string.aiInformation_segment_dataSharedWithModel_value),
                    ),
                ),
            ),
            NutritionFactBlock(
                blockTitle = stringResource(R.string.aiInformation_block_privacyCompliance),
                segments = listOf(
                    NutritionFactSegment(
                        segmentTitle = stringResource(R.string.aiInformation_segment_dataRetention_title),
                        description = stringResource(R.string.aiInformation_segment_dataRetention_description),
                        value = stringResource(R.string.aiInformation_segment_dataRetention_value),
                    ),
                    NutritionFactSegment(
                        segmentTitle = stringResource(R.string.aiInformation_segment_dataLogging_title),
                        description = stringResource(R.string.aiInformation_segment_dataLogging_description),
                        value = stringResource(R.string.aiInformation_segment_dataLogging_value),
                    ),
                    NutritionFactSegment(
                        segmentTitle = stringResource(R.string.aiInformation_segment_regionsSupported_title),
                        description = stringResource(R.string.aiInformation_segment_regionsSupported_description),
                        value = stringResource(R.string.aiInformation_segment_regionsSupported_value),
                    ),
                    NutritionFactSegment(
                        segmentTitle = stringResource(R.string.aiInformation_segment_pii_title),
                        description = stringResource(R.string.aiInformation_segment_pii_description),
                        value = stringResource(R.string.aiInformation_segment_pii_value),
                    ),
                ),
            ),
            NutritionFactBlock(
                blockTitle = stringResource(R.string.aiInformation_block_outputs),
                segments = listOf(
                    NutritionFactSegment(
                        segmentTitle = stringResource(R.string.aiInformation_segment_aiSettingsControl_title),
                        description = stringResource(R.string.aiInformation_segment_aiSettingsControl_description),
                        value = stringResource(R.string.aiInformation_segment_value),
                    ),
                    NutritionFactSegment(
                        segmentTitle = stringResource(R.string.aiInformation_segment_humanInTheLoop_title),
                        description = stringResource(R.string.aiInformation_segment_humanInTheLoop_description),
                        value = stringResource(R.string.aiInformation_segment_humanInTheLoop_value),
                    ),
                    NutritionFactSegment(
                        segmentTitle = stringResource(R.string.aiInformation_segment_guardrails_title),
                        description = stringResource(R.string.aiInformation_segment_guardrails_description),
                        value = stringResource(R.string.aiInformation_segment_guardrails_value),
                    ),
                    NutritionFactSegment(
                        segmentTitle = stringResource(R.string.aiInformation_segment_expectedRisks_title),
                        description = stringResource(R.string.aiInformation_segment_expectedRisks_description),
                        value = stringResource(R.string.aiInformation_segment_expectedRisks_value),
                    ),
                    NutritionFactSegment(
                        segmentTitle = stringResource(R.string.aiInformation_segment_intendedOutcomes_title),
                        description = stringResource(R.string.aiInformation_segment_intendedOutcomes_description),
                        value = stringResource(R.string.aiInformation_segment_intendedOutcomes_value),
                    ),
                ),
            ),
        ),
    ),
)

@Composable
internal fun AiInformationHeader(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ai_filled),
            contentDescription = null,
            tint = HorizonColors.Surface.aiGradientStart(),
            modifier = Modifier.size(20.dp),
        )
        HorizonSpace(SpaceSize.SPACE_4)
        Text(
            text = stringResource(R.string.igniteAIToolbarTitle),
            style = HorizonTypography.labelLargeBold,
            color = HorizonColors.Surface.aiGradientStart(),
            modifier = Modifier.weight(1f),
        )
        IconButton(
            iconRes = R.drawable.close,
            contentDescription = stringResource(R.string.igniteAIDismissContentDescription),
            size = IconButtonSize.SMALL,
            color = IconButtonColor.Ghost,
            onClick = onDismiss,
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun AiInformationScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    AiInformationScreen(
        data = previewAiInformationData(),
        onDismiss = {},
    )
}

internal fun previewAiInformationData() = AiInformationData(
    title = "Study Tools",
    permissionLevelText = "Permission Level",
    permissionLevel = "LEVEL 2",
    description = "We utilise off-the-shelf AI models and customer data as input to provide AI-powered features. No data is used for training this model.",
    permissionLevelsModalTriggerText = "View Permission Levels",
    modelNameText = "Base Model",
    modelName = "Claude 3.5 Haiku by Anthropic and Cohere multi-language v3",
    nutritionFactsModalTriggerText = "View AI Nutrition Facts",
    permissionLevelsData = AiInformationPermissionLevelsData(
        title = "Data Permission Levels",
        currentFeatureText = "Current Feature:",
        currentFeature = "Study Tools",
        closeButtonText = "Close",
        levels = listOf(
            DataPermissionLevel(
                level = "LEVEL 1",
                title = "Descriptive Analytics and Research",
                description = "We leverage anonymised aggregate data for detailed analytics to inform model development and product improvements. No AI models are used at this level.",
            ),
            DataPermissionLevel(
                level = "LEVEL 2",
                title = "AI-Powered Features Without Data Training",
                description = "We utilise off-the-shelf AI models and customer data as input to provide AI-powered features. No data is used for training this model.",
                isHighlighted = true,
            ),
            DataPermissionLevel(
                level = "LEVEL 3",
                title = "AI Customization for Individual Institutions",
                description = "We customise AI solutions tailored to the unique needs and resources of educational institutions.",
            ),
            DataPermissionLevel(
                level = "LEVEL 4",
                title = "Collaborative AI Consortium",
                description = "We established a consortium with educational institutions that shares anonymised data, best practices, and research findings.",
            ),
        ),
    ),
    nutritionFactsData = AiInformationNutritionFactsData(
        title = "Nutrition Facts",
        featureName = "Study Tools",
        closeButtonText = "Close",
        blocks = listOf(
            NutritionFactBlock(
                blockTitle = "Model & Data",
                segments = listOf(
                    NutritionFactSegment(
                        segmentTitle = "Base Model",
                        description = "The foundational AI on which further training and customizations are built.",
                        value = "Claude 3.5 Haiku by Anthropic and Cohere multi-language v3",
                    ),
                    NutritionFactSegment(
                        segmentTitle = "Trained with User Data",
                        description = "Indicates the AI model has been given customer data in order to improve its results.",
                        value = "No",
                    ),
                    NutritionFactSegment(
                        segmentTitle = "Data Shared with Model",
                        description = "Indicates which training or operational content was given to the model.",
                        value = "Course content",
                    ),
                ),
            ),
            NutritionFactBlock(
                blockTitle = "Privacy & Compliance",
                segments = listOf(
                    NutritionFactSegment(
                        segmentTitle = "Data Retention",
                        description = "How long the model stores customer data.",
                        value = "No",
                    ),
                    NutritionFactSegment(
                        segmentTitle = "Data Logging",
                        description = "Recording the AI's performance for auditing, analysis, and improvement.",
                        value = "No",
                    ),
                    NutritionFactSegment(
                        segmentTitle = "Regions Supported",
                        description = "The locations where the AI model is officially available and supported.",
                        value = "Global",
                    ),
                    NutritionFactSegment(
                        segmentTitle = "PII",
                        description = "Sensitive data that can be used to identify an individual.",
                        value = "No",
                    ),
                ),
            ),
        ),
    ),
)
