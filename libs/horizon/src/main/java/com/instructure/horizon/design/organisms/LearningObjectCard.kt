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
package com.instructure.horizon.design.organisms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.design.foundation.Colors
import com.instructure.horizon.design.molecules.ButtonSecondary
import com.instructure.horizon.design.molecules.Pill
import com.instructure.horizon.design.molecules.PillStyle
import java.util.Date

data class LearningObjectCardState(
    val moduleTitle: String,
    val learningObjectTitle: String,
    val progressLabel: String? = null,
    val remainingTime: String? = null,
    val learningObjectType: String? = null,
    val dueDate: Date? = null,
    val onClick: (() -> Unit)? = null
)

@Composable
fun LearningObjectCard(learningObjectCardState: LearningObjectCardState, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors().copy(containerColor = Colors.Surface.cardPrimary()),
        elevation = CardDefaults.elevatedCardElevation(),
        modifier = modifier
    ) {
        Column(Modifier.padding(36.dp)) {
            if (learningObjectCardState.progressLabel != null) Pill(PillStyle.OUTLINE, learningObjectCardState.progressLabel)
            Spacer(modifier = Modifier.padding(16.dp))
            Text(text = learningObjectCardState.moduleTitle)
            Spacer(modifier = Modifier.padding(4.dp))
            Text(text = learningObjectCardState.learningObjectTitle)
            Spacer(Modifier.padding(48.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (learningObjectCardState.remainingTime != null) {
                        Pill(PillStyle.INLINE, learningObjectCardState.remainingTime, iconRes = R.drawable.schedule)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (learningObjectCardState.learningObjectType != null) {
                        // TODO This icon should change based on the learning object type
                        Pill(PillStyle.INLINE, learningObjectCardState.learningObjectType, iconRes = R.drawable.text_snippet)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (learningObjectCardState.dueDate != null) {
                        // TODO How should we format the date?
                        Pill(PillStyle.INLINE, "Due ${learningObjectCardState.dueDate}", iconRes = R.drawable.calendar_today)
                    }
                }
                Spacer(modifier = Modifier.width(24.dp))
                ButtonSecondary(iconRes = R.drawable.arrow_forward)
            }
        }
    }
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
            learningObjectType = "Assignment",
            dueDate = Date(),
            onClick = {})
    )
}