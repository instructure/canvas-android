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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Badge
import com.instructure.horizon.horizonui.molecules.BadgeContent
import com.instructure.horizon.horizonui.molecules.BadgeType
import com.instructure.horizon.horizonui.organisms.Modal
import com.instructure.horizon.horizonui.organisms.ModalDialogState
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCard
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCardState
import com.instructure.horizon.model.LearningObjectStatus
import com.instructure.horizon.model.LearningObjectType

@Composable
fun ModalScreen() {
    var modalIndex by remember { mutableStateOf(-1) }
    if (modalIndex == 0) Modal(
        dialogState = ModalDialogState(
            title = "Title",
            message = "Long text message that will be displayed in the dialog. This is a preview message. ",
            primaryButtonTitle = "Primary",
        ), onDismiss = {
            modalIndex = -1
        }
    )

    if (modalIndex == 1) Modal(
        dialogState = ModalDialogState(
            title = "Title",
            message = "Long text message that will be displayed in the dialog. This is a preview message. ",
            primaryButtonTitle = "Primary",
            secondaryButtonTitle = "Secondary",
            secondaryButtonClick = {}
        ), onDismiss =  {
            modalIndex = -1
        }
    )

    if (modalIndex == 2) Modal(
        dialogState = ModalDialogState(
            title = "Title",
            message = "Long text message that will be displayed in the dialog. This is a preview message. ",
            primaryButtonTitle = "Primary",
            secondaryButtonTitle = "Secondary",
            secondaryButtonClick = {}
        ), onDismiss =  {
            modalIndex = -1
        },
        headerIcon = {
            Badge(type = BadgeType.Success, content = BadgeContent.Icon(R.drawable.check, null))
        }
    )

    if (modalIndex == 3) Modal(
        dialogState = ModalDialogState(
            title = "Title\nSubtitle",
            message = "Long text message that will be displayed in the dialog. This is a preview message. ",
            primaryButtonTitle = "Primary",
            secondaryButtonTitle = "Secondary",
            secondaryButtonClick = {}
        ), onDismiss =  {
            modalIndex = -1
        },
        headerIcon = {
            Badge(type = BadgeType.Success, content = BadgeContent.Icon(R.drawable.check, null))
        }
    )
    if (modalIndex == 4) Modal(
        dialogState = ModalDialogState(
            title = "Title",
            message = "Long text message that will be displayed in the dialog. This is a preview message. ",
            primaryButtonTitle = "Primary",
            secondaryButtonTitle = "Secondary",
            secondaryButtonClick = {}
        ), onDismiss =  {
            modalIndex = -1
        },
        headerIcon = {
            Badge(type = BadgeType.Success, content = BadgeContent.Icon(R.drawable.check, null))
        },
        extraBody = {
            ModuleItemCard(
                ModuleItemCardState(
                    title = "Title",
                    learningObjectType = LearningObjectType.ASSIGNMENT,
                    learningObjectStatus = LearningObjectStatus.REQUIRED,
                    onClick = {}
                )
            )
        }
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizonSpace(SpaceSize.SPACE_24)
        Text("Show one button modal", style = HorizonTypography.p1, modifier = Modifier.clickable { modalIndex = 0 })
        Text("Show two button modal", style = HorizonTypography.p1, modifier = Modifier.clickable { modalIndex = 1 })
        Text("Show icon modal", style = HorizonTypography.p1, modifier = Modifier.clickable { modalIndex = 2 })
        Text("Show multiline modal", style = HorizonTypography.p1, modifier = Modifier.clickable { modalIndex = 3 })
        Text("Show modal with extra content", style = HorizonTypography.p1, modifier = Modifier.clickable { modalIndex = 4 })
    }
}