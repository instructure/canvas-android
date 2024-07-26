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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.instructure.pandautils.utils.ThemePrefs

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
                    actions = {
                        if (uiState.isSending) {
                            CircularProgressIndicator(
                                color = Color(ThemePrefs.brandColor),
                                modifier = Modifier
                                    .padding(4.dp),
                            )
                        } else {
                            IconButton(
                                onClick = { actionHandler(InboxComposeActionHandler.SendClicked) },
                                enabled = uiState.isSendButtonEnabled,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_send),
                                    contentDescription = stringResource(R.string.a11y_send_message),
                                    tint = colorResource(id = R.color.textLightest).copy(alpha = LocalContentAlpha.current)
                                )
                            }
                        }
                    },
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
                        label = stringResource(id = R.string.course),
                        value = uiState.selectedContext?.name ?: "",
                        onClick = { actionHandler(InboxComposeActionHandler.OpenContextPicker) },
                    )

                    AnimatedVisibility(visible = uiState.selectedContext != null) {
                        LabelMultipleValuesRow(
                            label = stringResource(R.string.recipients_to),
                            selectedValues = uiState.selectedRecipients,
                            itemComposable = {
                                RecipientChip(it) {
                                    actionHandler(InboxComposeActionHandler.RemoveRecipient(it))
                                }
                            },
                            onSelect = { },
                            addValueClicked = { actionHandler(InboxComposeActionHandler.OpenRecipientPicker) },
                        )
                    }

                    CanvasDivider()

                    LabelSwitchRow(
                        label = stringResource(R.string.send_individual_message_to_each_recipient),
                        checked = uiState.sendIndividual,
                        onCheckedChange = {
                            actionHandler(InboxComposeActionHandler.SendIndividualChanged(it))
                        },
                    )

                    CanvasDivider()

                    LabelTextFieldRow(
                        value = uiState.subject,
                        label = stringResource(R.string.subject),
                        onValueChange = {
                            actionHandler(InboxComposeActionHandler.SubjectChanged(it))
                        },
                    )

                    CanvasDivider()
                    
                    TextFieldWithHeader(
                        label = stringResource(R.string.message),
                        value = uiState.body,
                        headerIconResource = R.drawable.ic_attachment,
                        onValueChange = {
                            actionHandler(InboxComposeActionHandler.BodyChanged(it))
                        },
                    )
                }
            }
        )
    }
}