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
package com.instructure.horizon.features.aiassistant.aiinformation.nutritionfacts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.features.aiassistant.aiinformation.AiInformationHeader
import com.instructure.horizon.features.aiassistant.aiinformation.model.AiInformationNutritionFactsData
import com.instructure.horizon.features.aiassistant.aiinformation.model.NutritionFactBlock
import com.instructure.horizon.features.aiassistant.aiinformation.model.NutritionFactSegment
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor

@Composable
fun AiInformationNutritionFactsScreen(
    data: AiInformationNutritionFactsData,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HorizonColors.Surface.pageSecondary())
                .statusBarsPadding(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                AiInformationHeader(onDismiss = onDismiss)
                HorizonSpace(SpaceSize.SPACE_8)
                Text(
                    text = data.title,
                    style = HorizonTypography.h2,
                    color = HorizonColors.Text.title(),
                )
            }
            HorizontalDivider(thickness = 1.dp, color = HorizonColors.LineAndBorder.lineStroke())
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item {
                    Text(
                        text = data.featureName,
                        style = HorizonTypography.h2,
                        color = HorizonColors.Text.title(),
                    )
                }
                items(data.blocks) { block ->
                    NutritionFactBlockSection(block = block)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    label = data.closeButtonText,
                    color = ButtonColor.Black,
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun NutritionFactBlockSection(
    block: NutritionFactBlock,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = block.blockTitle,
            style = HorizonTypography.h3,
            color = HorizonColors.Text.title(),
        )
        HorizonSpace(SpaceSize.SPACE_16)
        block.segments.forEach { segment ->
            NutritionFactSegmentCard(segment = segment)
            HorizonSpace(SpaceSize.SPACE_16)
        }
    }
}

@Composable
private fun NutritionFactSegmentCard(
    segment: NutritionFactSegment,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HorizonCornerRadius.level2)
            .border(
                width = 1.dp,
                color = HorizonColors.LineAndBorder.lineStroke(),
                shape = HorizonCornerRadius.level2,
            )
            .padding(16.dp),
    ) {
        Text(
            text = segment.segmentTitle,
            style = HorizonTypography.labelLargeBold,
            color = HorizonColors.Text.title(),
        )
        HorizonSpace(SpaceSize.SPACE_4)
        Text(
            text = segment.description,
            style = HorizonTypography.p2,
            color = HorizonColors.Surface.attention(),
        )
        HorizonSpace(SpaceSize.SPACE_8)
        Text(
            text = segment.value,
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body(),
        )
        if (segment.valueDescription != null) {
            HorizonSpace(SpaceSize.SPACE_4)
            Text(
                text = segment.valueDescription,
                style = HorizonTypography.p2,
                color = HorizonColors.Text.body(),
            )
        }
    }
}

@Composable
@Preview
private fun AiInformationNutritionFactsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    AiInformationNutritionFactsScreen(
        data = AiInformationNutritionFactsData(
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
                    ),
                ),
            ),
        ),
        onDismiss = {},
    )
}
