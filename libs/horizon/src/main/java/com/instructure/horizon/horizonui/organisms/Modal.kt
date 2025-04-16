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
package com.instructure.horizon.horizonui.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Badge
import com.instructure.horizon.horizonui.molecules.BadgeContent
import com.instructure.horizon.horizonui.molecules.BadgeType
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCard
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCardState
import com.instructure.horizon.model.LearningObjectStatus
import com.instructure.horizon.model.LearningObjectType

data class ModalDialogState(
    val title: String,
    val message: String,
    val primaryButtonTitle: String,
    val secondaryButtonTitle: String? = null,
    val primaryButtonClick: () -> Unit = {},
    val secondaryButtonClick: (() -> Unit)? = null
)

@Composable
fun Modal(
    dialogState: ModalDialogState,
    modifier: Modifier = Modifier,
    headerIcon: @Composable (() -> Unit)? = null,
    extraBody: @Composable (ColumnScope.() -> Unit)? = null,
    onDismiss: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(modifier = modifier.background(color = HorizonColors.Surface.pageSecondary(), shape = HorizonCornerRadius.level4)) {
            DialogHeader(title = dialogState.title, headerIcon = headerIcon, onDismiss = onDismiss)
            HorizontalDivider(thickness = 1.dp, color = HorizonColors.LineAndBorder.lineStroke())
            DialogBody(message = dialogState.message, extraBody = extraBody)
            DialogFooter(
                primaryButtonTitle = dialogState.primaryButtonTitle,
                secondaryButtonTitle = dialogState.secondaryButtonTitle,
                primaryButtonClick = dialogState.primaryButtonClick,
                secondaryButtonClick = dialogState.secondaryButtonClick
            )
        }
    }
}

@Composable
private fun DialogHeader(
    title: String,
    modifier: Modifier = Modifier,
    headerIcon: @Composable (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Row(modifier = modifier.padding(24.dp)) {
        if (headerIcon != null) {
            Box(modifier = Modifier.size(height = 28.dp, width = 24.dp), contentAlignment = Alignment.Center) { headerIcon() }
            HorizonSpace(SpaceSize.SPACE_8)
        }
        Text(text = title, style = HorizonTypography.h3, modifier = Modifier.weight(1f))
        IconButton(
            iconRes = R.drawable.close,
            size = IconButtonSize.SMALL,
            color = IconButtonColor.INVERSE,
            elevation = HorizonElevation.level4,
            onClick = onDismiss
        )
    }
}

@Composable
private fun DialogBody(
    message: String,
    modifier: Modifier = Modifier,
    extraBody: @Composable (ColumnScope.() -> Unit)? = null
) {
    Column(modifier = modifier.padding(24.dp)) {
        Text(text = message, style = HorizonTypography.p1)
        if (extraBody != null) {
            HorizonSpace(SpaceSize.SPACE_24)
            extraBody()
        }
    }
}

@Composable
private fun DialogFooter(
    primaryButtonTitle: String,
    secondaryButtonTitle: String?,
    primaryButtonClick: () -> Unit,
    secondaryButtonClick: (() -> Unit)?
) {
    Row(modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
        Spacer(Modifier.weight(1f))
        if (secondaryButtonTitle != null && secondaryButtonClick != null) {
            Button(label = secondaryButtonTitle, onClick = secondaryButtonClick, color = ButtonColor.Inverse)
            HorizonSpace(SpaceSize.SPACE_8)
        }
        Button(label = primaryButtonTitle, onClick = primaryButtonClick, color = ButtonColor.Institution)
    }
}

@Composable
@Preview
private fun ModalPreviewOneButton() {
    ContextKeeper.appContext = LocalContext.current
    Modal(
        dialogState = ModalDialogState(
            title = "Title",
            message = "Long text message that will be displayed in the dialog. This is a preview message. ",
            primaryButtonTitle = "Primary"
        )
    )
}

@Composable
@Preview
private fun ModalPreviewTwoButton() {
    ContextKeeper.appContext = LocalContext.current
    Modal(
        dialogState = ModalDialogState(
            title = "Title",
            message = "Long text message that will be displayed in the dialog. This is a preview message. ",
            primaryButtonTitle = "Primary",
            secondaryButtonTitle = "Secondary",
            secondaryButtonClick = {}
        )
    )
}

@Composable
@Preview
private fun ModalPreviewWithIcon() {
    ContextKeeper.appContext = LocalContext.current
    Modal(
        dialogState = ModalDialogState(
            title = "Title",
            message = "Long text message that will be displayed in the dialog. This is a preview message. ",
            primaryButtonTitle = "Primary",
            secondaryButtonTitle = "Secondary",
            secondaryButtonClick = {}
        ),
        headerIcon = {
            Badge(type = BadgeType.Success, content = BadgeContent.Icon(R.drawable.check, null))
        }
    )
}

@Composable
@Preview
private fun ModalPreviewWithMultilineTitle() {
    ContextKeeper.appContext = LocalContext.current
    Modal(
        dialogState = ModalDialogState(
            title = "Title\nSubtitle",
            message = "Long text message that will be displayed in the dialog. This is a preview message. ",
            primaryButtonTitle = "Primary",
            secondaryButtonTitle = "Secondary",
            secondaryButtonClick = {}
        ),
        headerIcon = {
            Badge(type = BadgeType.Success, content = BadgeContent.Icon(R.drawable.check, null))
        }
    )
}

@Composable
@Preview
private fun ModalPreviewWithExtraBody() {
    ContextKeeper.appContext = LocalContext.current
    Modal(
        dialogState = ModalDialogState(
            title = "Title",
            message = "Long text message that will be displayed in the dialog. This is a preview message. ",
            primaryButtonTitle = "Primary",
            secondaryButtonTitle = "Secondary",
            secondaryButtonClick = {}
        ),
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
}