/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.features.inbox.compose.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.instructure.canvasapi2.models.Attachment
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.LabelSwitchRow
import com.instructure.pandautils.compose.composables.LabelTextFieldRow
import com.instructure.pandautils.compose.composables.LabelValueRow
import com.instructure.pandautils.compose.composables.TextFieldWithHeader
import com.instructure.pandautils.features.inbox.compose.InboxComposeActionHandler
import com.instructure.pandautils.features.inbox.compose.InboxComposeUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InboxComposeScreen(
    title: String,
    uiState: InboxComposeUiState,
    actionHandler: (InboxComposeActionHandler) -> Unit,
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasThemedAppBar(
                    title = title,
                    navigationActionClick = { actionHandler(InboxComposeActionHandler.CancelClicked) },
                )
            },
            content = { padding ->
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .imeNestedScroll()
                        .padding(padding)
                ) {
                    LabelValueRow(
                        label = "Course",
                        value = uiState.selectedContext?.name ?: "",
                        onClick = { actionHandler(InboxComposeActionHandler.OpenContextPicker) },
                    )

                    if (uiState.selectedContext != null) {
                        LabelMultipleValuesRow(
                            label = "To",
                            selectedValues = uiState.selectedRecipients,
                            itemComposable = { RecipientChip(it) },
                            onSelect = {},
                            addValueClicked = { actionHandler(InboxComposeActionHandler.OpenRecipientPicker) },
                        )
                    }

                    CanvasDivider()

                    LabelSwitchRow(
                        label = "Send individual message to each recipient",
                        checked = uiState.sendIndividual,
                        onCheckedChange = { uiState.sendIndividual = it },
                    )

                    CanvasDivider()

                    LabelTextFieldRow(
                        label = "Subject",
                        onValueChange = {
                            uiState.subject = it
                        },
                    )

                    CanvasDivider()
                    
                    TextFieldWithHeader(
                        label = "Message",
                        headerIconResource = R.drawable.ic_attachment,
                        onValueChange = {
                            uiState.body = it
                        },
                    )

                    AttachmentCard(
                        Attachment(
                            id = 1,
                            contentType = "image/png",
                            filename = "image.png",
                            displayName = "image.png",
                            url = "https://www.example.com/image.png",
                            thumbnailUrl = null,
                            previewUrl = null,
                            size = 1024
                        ),
                        AttachmentStatus.UPLOADED,
                        {},
                        LocalContext.current
                    )
                }
            }
        )
    }
}