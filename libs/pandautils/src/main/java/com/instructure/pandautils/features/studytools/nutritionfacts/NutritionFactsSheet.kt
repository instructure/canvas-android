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
 */
package com.instructure.pandautils.features.studytools.nutritionfacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.container.Tray
import com.instructure.instui.compose.indicator.Icon as InstUIIcon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.token.component.InstUIText as InstUITextTokens
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.x
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.instui.compose.text.Text as InstUIText

/**
 * The AI disclosure ("Nutrition Facts"), opened from the Study Tools header's help icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionFactsSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val data = studyToolsNutritionFacts()

    InstUITheme {
        Tray(
            onDismiss = onDismiss,
            modifier = modifier.testTag("nutritionFactsSheet")
        ) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = InstUILayoutSizes.Spacing.SpaceXl.spaceXl,
                    end = InstUILayoutSizes.Spacing.SpaceXl.spaceXl,
                    bottom = InstUILayoutSizes.Spacing.Space2xl.space2xl
                )
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Heading(
                                text = data.title,
                                level = HeadingLevel.H2,
                                modifier = Modifier.semantics { heading() }
                            )
                            InstUIText(
                                text = data.featureName,
                                style = InstUITextTokens.contentSmall,
                                color = InstUISemanticColors.Text.muted()
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .defaultMinSize(
                                    minWidth = InstUILayoutSizes.Size.Interactive.height_lg,
                                    minHeight = InstUILayoutSizes.Size.Interactive.height_lg
                                )
                                // Clipped to a circle so the ripple matches the round icon instead of the square box.
                                .clip(CircleShape)
                                .clickable(onClick = onDismiss)
                                .semantics { role = Role.Button }
                                .testTag("closeNutritionFacts")
                        ) {
                            InstUIIcon(
                                imageVector = InstUIIcons.x,
                                contentDescription = stringResource(id = R.string.close),
                                size = IconSize.Medium,
                                color = IconColor.Base
                            )
                        }
                    }
                }

                data.blocks.forEach { block ->
                    item {
                        Heading(
                            text = block.blockTitle,
                            level = HeadingLevel.H4,
                            modifier = Modifier
                                .padding(top = InstUILayoutSizes.Spacing.SpaceXl.spaceXl, bottom = InstUILayoutSizes.Spacing.SpaceSm.spaceSm)
                                .semantics { heading() }
                        )
                    }
                    items(block.segments) { segment ->
                        NutritionFactRow(segment)
                        CanvasDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionFactRow(segment: NutritionFactSegment) {
    Column(
        verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
        modifier = Modifier
            .fillMaxWidth()
            .background(InstUISemanticColors.Background.container())
            .padding(vertical = InstUILayoutSizes.Spacing.SpaceMd.spaceMd)
    ) {
        InstUIText(
            text = segment.segmentTitle,
            style = InstUITextTokens.contentImportant,
            color = InstUISemanticColors.Text.base()
        )
        InstUIText(
            text = segment.description,
            style = InstUITextTokens.contentSmall,
            color = InstUISemanticColors.Text.muted()
        )
        InstUIText(
            text = segment.value,
            style = InstUITextTokens.contentSmall,
            color = InstUISemanticColors.Text.base()
        )
    }
}
