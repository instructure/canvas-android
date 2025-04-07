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
@file:OptIn(ExperimentalLayoutApi::class)

package com.instructure.horizon.horizonui.organisms.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.model.LearningObjectStatus
import com.instructure.horizon.model.LearningObjectType
import com.instructure.pandautils.utils.formatDayMonth
import java.util.Date

data class ModuleItemCardState(
    val title: String,
    val learningObjectType: LearningObjectType,
    val learningObjectStatus: LearningObjectStatus,
    val locked: Boolean = false,
    val selected: Boolean = false,
    val remainingTime: String? = null,
    val dueDate: Date? = null,
    val pastDue: Boolean = false, // TODO: I am not sure if this should be handled here, we will revisit this once we have real data
    val points: String? = null,
    val onClick: (() -> Unit)? = null
)

@Composable
fun ModuleItemCard(state: ModuleItemCardState, modifier: Modifier = Modifier) {
    val onClick = state.onClick
    Card(
        shape = HorizonCornerRadius.level2,
        colors = CardDefaults.cardColors().copy(containerColor = HorizonColors.Surface.cardPrimary()),
        border = if (state.selected) HorizonBorder.level2(HorizonColors.Surface.institution()) else HorizonBorder.level1(),
        modifier = modifier
    ) {
        val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
        Row(verticalAlignment = Alignment.CenterVertically, modifier = clickModifier) {
            Column(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .weight(1f)
            ) {
                Text(text = state.title, style = HorizonTypography.p2)
                HorizonSpace(SpaceSize.SPACE_12)
                FlowRow(verticalArrangement = Arrangement.spacedBy(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LearningObjectTypePill(state.learningObjectType, modifier = Modifier.align(Alignment.CenterVertically))
                    ModuleItemText(
                        text = stringResource(state.learningObjectStatus.stringRes),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    if (state.remainingTime != null) ModuleItemText(
                        text = state.remainingTime,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    if (state.dueDate != null) {
                        val dueStringRes = if (state.pastDue) R.string.learningobject_pastDue else R.string.learningobject_dueDate
                        val dueColor = if (state.pastDue) HorizonColors.Text.error() else HorizonColors.Text.timestamp()
                        ModuleItemText(
                            text = stringResource(
                                dueStringRes,
                                state.dueDate.formatDayMonth()
                            ),
                            modifier = Modifier.align(Alignment.CenterVertically),
                            color = dueColor
                        )
                    }
                    if (state.points != null) ModuleItemText(text = state.points, modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
            ModuleItemCardIcon(state = state, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun RowScope.ModuleItemCardIcon(state: ModuleItemCardState, modifier: Modifier = Modifier) {
    when {
        state.locked -> {
            HorizonSpace(SpaceSize.SPACE_8)
            Icon(
                painterResource(R.drawable.lock),
                contentDescription = null,
                tint = HorizonColors.Surface.institution(),
                modifier = modifier
            )
        }

        state.learningObjectStatus.completed -> {
            HorizonSpace(SpaceSize.SPACE_8)
            Icon(
                painterResource(R.drawable.check_circle_full),
                contentDescription = null,
                tint = HorizonColors.Surface.institution(),
                modifier = modifier
            )
        }

        state.learningObjectStatus == LearningObjectStatus.REQUIRED -> {
            HorizonSpace(SpaceSize.SPACE_8)
            Icon(
                painterResource(R.drawable.circle),
                contentDescription = null,
                tint = HorizonColors.LineAndBorder.lineStroke(),
                modifier = modifier
            )
        }
    }
}

@Composable
@Preview
fun ModuleItemCardOptionalPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemCard(
        ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.ASSIGNMENT,
            learningObjectStatus = LearningObjectStatus.OPTIONAL,
            locked = false,
            remainingTime = "30 min",
            dueDate = Date(),
            points = "10 pts"
        )
    )
}

@Composable
@Preview
fun ModuleItemCardRequiredPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemCard(
        ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.ASSIGNMENT,
            learningObjectStatus = LearningObjectStatus.REQUIRED,
            locked = false,
            remainingTime = "30 min",
            dueDate = Date(),
            points = "10 pts"
        )
    )
}

@Composable
@Preview
fun ModuleItemCardViewedPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemCard(
        ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.ASSIGNMENT,
            learningObjectStatus = LearningObjectStatus.VIEWED,
            locked = false,
            remainingTime = "30 min",
            dueDate = Date(),
            points = "10 pts"
        )
    )
}

@Composable
@Preview
fun ModuleItemCardSubmittedPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemCard(
        ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.ASSIGNMENT,
            learningObjectStatus = LearningObjectStatus.SUBMITTED,
            locked = false,
            remainingTime = "30 min",
            dueDate = Date(),
            points = "10 pts"
        )
    )
}

@Composable
@Preview
fun ModuleItemCardLockedPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemCard(
        ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.ASSIGNMENT,
            learningObjectStatus = LearningObjectStatus.REQUIRED,
            locked = true,
            remainingTime = "30 min",
            dueDate = Date(),
            points = "10 pts"
        )
    )
}

@Composable
@Preview
fun ModuleItemCardSelectedPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemCard(
        ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.ASSIGNMENT,
            learningObjectStatus = LearningObjectStatus.VIEWED,
            locked = false,
            remainingTime = "30 min",
            dueDate = Date(),
            points = "10 pts",
            selected = true
        )
    )
}

@Composable
@Preview
fun ModuleItemCardPastDuePreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemCard(
        ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.ASSIGNMENT,
            learningObjectStatus = LearningObjectStatus.VIEWED,
            locked = false,
            remainingTime = "30 min",
            dueDate = Date(),
            points = "10 pts",
            pastDue = true
        )
    )
}

@Composable
@Preview
fun ModuleItemCardWithoutOptionalFieldsPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemCard(
        ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.ASSIGNMENT,
            learningObjectStatus = LearningObjectStatus.VIEWED
        )
    )
}

@Composable
@Preview
fun ModuleItemCardPagePreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemCard(
        ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.PAGE,
            learningObjectStatus = LearningObjectStatus.VIEWED
        )
    )
}

@Composable
@Preview
fun ModuleItemCardExternalLinkPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemCard(
        ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.EXTERNAL_URL,
            learningObjectStatus = LearningObjectStatus.VIEWED
        )
    )
}

@Composable
@Preview
fun ModuleItemCardExternalToolPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemCard(
        ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.EXTERNAL_TOOL,
            learningObjectStatus = LearningObjectStatus.VIEWED
        )
    )
}

@Composable
@Preview
fun ModuleItemCardFilePreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemCard(
        ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.FILE,
            learningObjectStatus = LearningObjectStatus.VIEWED
        )
    )
}

@Composable
@Preview
fun ModuleItemCardAssessmentPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemCard(
        ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.ASSESSMENT,
            learningObjectStatus = LearningObjectStatus.VIEWED
        )
    )
}