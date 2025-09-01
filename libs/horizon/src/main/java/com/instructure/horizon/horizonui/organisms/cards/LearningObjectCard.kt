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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.instructure.horizon.horizonui.foundation.horizonShadow
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillStyle
import com.instructure.horizon.horizonui.molecules.PillType
import com.instructure.horizon.model.LearningObjectType
import com.instructure.pandautils.utils.localisedFormatMonthDay
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
    Box(modifier = modifier
            .horizonShadow(HorizonElevation.level4, shape = HorizonCornerRadius.level2, clip = true)
            .clip(HorizonCornerRadius.level2)
            .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level2)
    ) {
        val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
        Column(clickModifier.padding(36.dp)) {
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
                        LearningObjectTypePill(learningObjectCardState.learningObjectType)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (learningObjectCardState.dueDate != null) {
                        LearningObjectPill(
                            stringResource(R.string.learningobject_dueDate, learningObjectCardState.dueDate.localisedFormatMonthDay()),
                            iconRes = R.drawable.calendar_today
                        )
                    }
                }
                Spacer(modifier = Modifier.width(24.dp))
                if (onClick != null) IconButton(iconRes = R.drawable.arrow_forward, color = IconButtonColor.Institution, onClick = onClick)
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