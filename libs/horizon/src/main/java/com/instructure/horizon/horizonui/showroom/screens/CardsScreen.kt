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
package com.instructure.horizon.horizonui.showroom.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.organisms.cards.LearningObjectCard
import com.instructure.horizon.horizonui.organisms.cards.LearningObjectCardState
import com.instructure.horizon.horizonui.organisms.cards.ModuleContainerPreview
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCard
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCardState
import com.instructure.horizon.horizonui.organisms.cards.ModuleStatus
import com.instructure.horizon.model.LearningObjectStatus
import com.instructure.horizon.model.LearningObjectType
import java.util.Date

@Composable
fun CardsScreen() {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Module Headers", style = HorizonTypography.p2)
        ModuleStatus.entries.forEachIndexed { index, status ->
            val even = index % 2 == 0
            val subtitle = "Long subtitle that is at least 2 lines long."
            var expanded by remember { mutableStateOf(false) }
            ModuleContainerPreview(status, pastDueCount = index % 3, expanded = expanded, subtitle = if (even) subtitle else null) {
                expanded = !expanded
            }
        }
        Text("Module Items", style = HorizonTypography.p2)
        LearningObjectType.entries.forEach { type ->
            LearningObjectStatus.entries.forEach { status ->
                ModuleItemCard(ModuleItemCardState(
                    title = "Module Item Title",
                    learningObjectType = type,
                    learningObjectStatus = status,
                ))
            }
        }
        ModuleItemCard(ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.ASSIGNMENT,
            learningObjectStatus = LearningObjectStatus.REQUIRED,
            locked = true
        ))
        ModuleItemCard(ModuleItemCardState(
            title = "Module Item Title",
            learningObjectType = LearningObjectType.ASSIGNMENT,
            learningObjectStatus = LearningObjectStatus.REQUIRED,
            selected = true
        ))

        Text("Learning Object Cards", style = HorizonTypography.p2)
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

        LearningObjectCard(
            LearningObjectCardState(
                moduleTitle = "Module Title",
                learningObjectTitle = "Learning Object Title",
                learningObjectType = LearningObjectType.PAGE,
                onClick = {})
        )
        HorizonSpace(SpaceSize.SPACE_8)
    }
}

@Composable
@Preview(showBackground = true)
fun CardsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    CardsScreen()
}