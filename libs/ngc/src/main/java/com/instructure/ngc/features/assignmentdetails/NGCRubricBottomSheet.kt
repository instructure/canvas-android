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
package com.instructure.ngc.features.assignmentdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.instructure.instui.compose.LocalCourseColor
import com.instructure.instui.compose.container.Card
import com.instructure.instui.compose.container.Elevation
import com.instructure.instui.compose.container.ToggleDetails
import com.instructure.instui.compose.container.Tray
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUISharedTokens
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R

/**
 * Read-only rubric bottom sheet for the NGC Assignment Details screen. Lists each criterion as a
 * collapsible [Card]; the rating the student was assessed at is highlighted with the course color.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RubricBottomSheet(rubric: NgcRubricUiState, onDismiss: () -> Unit) {
    Tray(onDismiss = onDismiss, modifier = Modifier.testTag("ngcAssignmentDetails_rubricSheet")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        ) {
            Heading(
                text = stringResource(R.string.ngc_rubric),
                level = HeadingLevel.H3,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
            )
            rubric.criteria.forEach { criterion -> CriterionCard(criterion) }
        }
    }
}

@Composable
private fun CriterionCard(criterion: NgcRubricCriterion) {
    var expanded by remember { mutableStateOf(true) }
    Card(modifier = Modifier.fillMaxWidth(), elevation = Elevation.Level1) {
        ToggleDetails(
            title = criterion.title,
            expanded = expanded,
            onExpandToggle = { expanded = !expanded },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd)) {
                criterion.description?.let { description ->
                    Text(text = description, style = InstUIText.contentSmall, color = InstUISemanticColors.Text.base())
                }
                Column(verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm)) {
                    criterion.ratings.forEach { rating -> RubricRatingRow(rating) }
                }
                criterion.assessedScore?.let { assessedScore ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.ngc_rubricScore),
                            style = InstUIText.contentImportant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = stringResource(R.string.ngc_rubricScoreValue, assessedScore, criterion.maxScore),
                            style = InstUIText.content,
                            color = InstUISemanticColors.Text.muted(),
                        )
                    }
                }
                // Teacher's per-criterion feedback, shown in a muted box at the bottom of the criterion.
                criterion.comment?.let { comment ->
                    Text(
                        text = comment,
                        style = InstUIText.contentSmall,
                        color = InstUISemanticColors.Text.base(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(InstUISharedTokens.BorderRadius.Card.md))
                            .background(InstUISemanticColors.Background.muted())
                            .padding(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
                    )
                }
            }
        }
    }
}

@Composable
private fun RubricRatingRow(rating: NgcRubricRating) {
    val courseColor = LocalCourseColor.current
    val isAssessed = rating.isAssessed
    // Assessed rating fills with the course color; everything else stays on the card background.
    val contentColor = if (isAssessed) InstUISemanticColors.Text.onColor() else InstUISemanticColors.Text.base()
    val detailColor = if (isAssessed) contentColor else InstUISemanticColors.Text.muted()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(InstUISharedTokens.BorderRadius.Card.md))
            .background(if (isAssessed) courseColor else Color.Transparent)
            .padding(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
        horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
    ) {
        rating.pointsLabel?.let { pointsLabel ->
            Text(
                text = pointsLabel,
                style = InstUIText.contentImportant,
                color = contentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = InstUILayoutSizes.Spacing.SpaceXl.spaceXl),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.Space2xs.space2xs),
        ) {
            rating.title?.let { title ->
                Text(text = title, style = InstUIText.contentImportant, color = contentColor)
            }
            rating.detail?.let { detail ->
                Text(text = detail, style = InstUIText.contentSmall, color = detailColor)
            }
        }
    }
}
