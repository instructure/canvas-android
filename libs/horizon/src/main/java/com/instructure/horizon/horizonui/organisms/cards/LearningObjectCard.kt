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
package com.instructure.horizon.horizonui.organisms.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.IconButtonSecondary
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillStyle
import com.instructure.horizon.horizonui.molecules.PillType
import com.instructure.horizon.model.LearningObjectType
import com.instructure.pandautils.utils.formatDayMonth
import java.util.Date

data class LearningObjectCardState(
    val moduleTitle: String,
    val learningObjectTitle: String,
    val progressLabel: String? = null,
    val remainingTime: String? = null,
    val learningObjectType: LearningObjectType? = null,
    val dueDate: Date? = null,
    val onClick: (() -> Unit)? = null
)

@Composable
fun LearningObjectCard(learningObjectCardState: LearningObjectCardState, modifier: Modifier = Modifier) {
    val onClick = learningObjectCardState.onClick
    val cardModifier = if (onClick != null) modifier.clickable { onClick() } else modifier
    Card(
        shape = HorizonCornerRadius.level2,
        colors = CardDefaults.cardColors().copy(containerColor = HorizonColors.Surface.cardPrimary()),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = HorizonElevation.level4,
            pressedElevation = HorizonElevation.level4,
            focusedElevation = HorizonElevation.level4,
            disabledElevation = HorizonElevation.level4,
            hoveredElevation = HorizonElevation.level4,
            draggedElevation = HorizonElevation.level4
        ),
        modifier = cardModifier
    ) {
        Column(Modifier.padding(36.dp)) {
            if (learningObjectCardState.progressLabel != null) Pill(learningObjectCardState.progressLabel)
            Spacer(modifier = Modifier.padding(16.dp))
            Text(text = learningObjectCardState.moduleTitle, style = HorizonTypography.p2, color = HorizonColors.Text.body())
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = learningObjectCardState.learningObjectTitle,
                style = HorizonTypography.h3,
                color = HorizonColors.Surface.institution()
            )
            Spacer(Modifier.padding(48.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (learningObjectCardState.remainingTime != null) {
                        LearningObjectPill(learningObjectCardState.remainingTime, iconRes = R.drawable.schedule)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (learningObjectCardState.learningObjectType != null) {
                        LearningObjectPill(
                            stringResource(learningObjectCardState.learningObjectType.stringRes),
                            iconRes = learningObjectCardState.learningObjectType.iconRes
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (learningObjectCardState.dueDate != null) {
                        LearningObjectPill(
                            stringResource(R.string.learningobject_dueDate, learningObjectCardState.dueDate.formatDayMonth()),
                            iconRes = R.drawable.calendar_today
                        )
                    }
                }
                Spacer(modifier = Modifier.width(24.dp))
                IconButtonSecondary(iconRes = R.drawable.arrow_forward)
            }
        }
    }
}

@Composable
private fun LearningObjectPill(label: String, iconRes: Int? = null) {
    Pill(label = label, style = PillStyle.INLINE, type = PillType.LEARNING_OBJECT_TYPE, case = PillCase.TITLE, iconRes = iconRes)
}

@Preview(showBackground = true)
@Composable
private fun LearningObjectCardPreview() {
    ContextKeeper.appContext = LocalContext.current
    LearningObjectCard(
        LearningObjectCardState(
            moduleTitle = "Module Title",
            learningObjectTitle = "Learning Object Title",
            progressLabel = "In progress",
            remainingTime = "30 min",
            learningObjectType = LearningObjectType.ASSIGNMENT,
            dueDate = Date(),
            onClick = {})
    )
}