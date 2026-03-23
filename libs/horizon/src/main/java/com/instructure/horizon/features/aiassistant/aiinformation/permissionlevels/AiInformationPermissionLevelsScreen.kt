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
package com.instructure.horizon.features.aiassistant.aiinformation.permissionlevels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.instructure.horizon.features.aiassistant.aiinformation.model.AiInformationPermissionLevelsData
import com.instructure.horizon.features.aiassistant.aiinformation.model.DataPermissionLevel
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor

@Composable
fun AiInformationPermissionLevelsScreen(
    data: AiInformationPermissionLevelsData,
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
                items(data.levels) { level ->
                    if (level.isHighlighted) {
                        HighlightedPermissionLevelItem(
                            level = level,
                            currentFeatureText = data.currentFeatureText,
                            currentFeature = data.currentFeature,
                        )
                    } else {
                        PermissionLevelItem(level = level)
                    }
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
private fun PermissionLevelItem(
    level: DataPermissionLevel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = level.level,
            style = HorizonTypography.tag,
            color = HorizonColors.PrimitivesViolet.violet40(),
        )
        HorizonSpace(SpaceSize.SPACE_4)
        Text(
            text = level.title,
            style = HorizonTypography.h4,
            color = HorizonColors.Text.title(),
        )
        HorizonSpace(SpaceSize.SPACE_8)
        Text(
            text = level.description,
            style = HorizonTypography.p1,
            color = HorizonColors.Surface.attention(),
        )
    }
}

@Composable
private fun HighlightedPermissionLevelItem(
    level: DataPermissionLevel,
    currentFeatureText: String,
    currentFeature: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(HorizonCornerRadius.level2)
            .border(
                width = 1.dp,
                color = HorizonColors.LineAndBorder.lineStroke(),
                shape = HorizonCornerRadius.level2,
            ),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = HorizonColors.Surface.aiGradient())
                    .padding(16.dp),
            ) {
                Text(
                    text = "$currentFeatureText $currentFeature",
                    style = HorizonTypography.labelLargeBold,
                    color = HorizonColors.Text.surfaceColored(),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HorizonColors.Surface.cardPrimary())
                    .padding(16.dp),
            ) {
                Text(
                    text = level.level,
                    style = HorizonTypography.tag,
                    color = HorizonColors.PrimitivesViolet.violet40(),
                )
                HorizonSpace(SpaceSize.SPACE_4)
                Text(
                    text = level.title,
                    style = HorizonTypography.h4,
                    color = HorizonColors.Text.title(),
                )
                HorizonSpace(SpaceSize.SPACE_8)
                Text(
                    text = level.description,
                    style = HorizonTypography.p1,
                    color = HorizonColors.Surface.attention(),
                )
            }
        }
    }
}

@Composable
@Preview
private fun AiInformationPermissionLevelsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    AiInformationPermissionLevelsScreen(
        data = AiInformationPermissionLevelsData(
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
        onDismiss = {},
    )
}
