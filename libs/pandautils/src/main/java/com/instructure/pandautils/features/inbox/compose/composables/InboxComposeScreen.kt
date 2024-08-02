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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.LabelSwitchRow
import com.instructure.pandautils.compose.composables.LabelTextFieldRow
import com.instructure.pandautils.compose.composables.SimpleAlertDialog
import com.instructure.pandautils.compose.composables.TextFieldWithHeader
import com.instructure.pandautils.features.inbox.compose.ContextPickerUiState
import com.instructure.pandautils.features.inbox.compose.InboxComposeActionHandler
import com.instructure.pandautils.features.inbox.compose.InboxComposeUiState
import com.instructure.pandautils.features.inbox.compose.RecipientPickerUiState
import com.instructure.pandautils.features.inbox.compose.ScreenState

@Composable
fun InboxComposeScreen(
    title: String,
    uiState: InboxComposeUiState,
    actionHandler: (InboxComposeActionHandler) -> Unit,
) {
    val subjectFocusRequester = remember { FocusRequester() }
    val bodyFocusRequester = remember { FocusRequester() }

    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasAppBar(
                    title = title,
                    navigationActionClick = { actionHandler(InboxComposeActionHandler.CancelDismissDialog(true)) },
                    actions = {
                        IconButton(
                            onClick = { actionHandler(InboxComposeActionHandler.SendClicked) },
                            enabled = uiState.isSendButtonEnabled,
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_send),
                                contentDescription = stringResource(R.string.a11y_sendMessage),
                                tint =
                                if (uiState.isSendButtonEnabled)
                                    colorResource(id = R.color.textDarkest)
                                else
                                    colorResource(id = R.color.textDarkest).copy(alpha = LocalContentAlpha.current),
                            )
                        }
                    },
                )
            },
            content = { padding ->
                Column(
                   modifier = Modifier.fillMaxSize()
                ) {
                    ContentScreen(padding, subjectFocusRequester, bodyFocusRequester, uiState, actionHandler)
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            bodyFocusRequester.requestFocus()
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun ContentScreen(
    padding: PaddingValues,
    subjectFocusRequester: FocusRequester,
    bodyFocusRequester: FocusRequester,
    uiState: InboxComposeUiState,
    actionHandler: (InboxComposeActionHandler) -> Unit,
) {
    if (uiState.showConfirmationDialog) {
        SimpleAlertDialog(
            dialogTitle = stringResource(id = R.string.exitWithoutSavingTitle),
            dialogText = stringResource(id = R.string.exitWithoutSavingMessage),
            dismissButtonText = stringResource(id = R.string.cancel),
            confirmationButtonText = stringResource(id = R.string.exitUnsaved),
            onDismissRequest = {
                actionHandler(InboxComposeActionHandler.CancelDismissDialog(false))
            },
            onConfirmation = {
                actionHandler(InboxComposeActionHandler.Close)
            }
        )
    }
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .fillMaxSize()
    ) {
        ContextValueRow(
            label = stringResource(id = R.string.course),
            value = uiState.contextPickerUiState.selectedContext,
            onClick = { actionHandler(InboxComposeActionHandler.OpenContextPicker) },
        )

        CanvasDivider()

        AnimatedVisibility(visible = uiState.contextPickerUiState.selectedContext != null) {
            Column {
                LabelMultipleValuesRow(
                    label = stringResource(R.string.recipientsTo),
                    selectedValues = uiState.recipientPickerUiState.selectedRecipients,
                    itemComposable = {
                        RecipientChip(it) {
                            actionHandler(InboxComposeActionHandler.RemoveRecipient(it))
                        }
                    },
                    addValueClicked = { actionHandler(InboxComposeActionHandler.OpenRecipientPicker) },
                )

                CanvasDivider()
            }
        }

        LabelSwitchRow(
            label = stringResource(R.string.sendIndividualMessageToEachRecipient),
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
            focusRequester = subjectFocusRequester,
        )

        CanvasDivider()

        TextFieldWithHeader(
            label = stringResource(R.string.message),
            value = uiState.body,
            headerIconResource = R.drawable.ic_attachment,
            onValueChange = {
                actionHandler(InboxComposeActionHandler.BodyChanged(it))
            },
            focusRequester = bodyFocusRequester,
            modifier = Modifier
                .defaultMinSize(minHeight = 100.dp)
        )
    }
}

@Preview
@Composable
fun InboxComposeScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val uiState = InboxComposeUiState(
        contextPickerUiState = ContextPickerUiState(
            selectedContext = Course(id = 1, name = "Course 1", courseColor = "#FF0000"),
        ),
        recipientPickerUiState = RecipientPickerUiState(
            selectedRecipients = listOf(Recipient(stringId = "1", name = "Person 1"), Recipient(stringId = "2", name = "Person 2")),
        ),
        sendIndividual = true,
        subject = TextFieldValue("Test Subject"),
        body = TextFieldValue("Test Body"),
        screenState = ScreenState.Data,
        showConfirmationDialog = false,
    )
    InboxComposeScreen(
        title = "New Message",
        uiState = uiState,
    ) {}
}

@Preview
@Composable
fun InboxComposeScreenConfirmDialogPreview() {
    ContextKeeper.appContext = LocalContext.current
    val uiState = InboxComposeUiState(
        contextPickerUiState = ContextPickerUiState(
            selectedContext = Course(id = 1, name = "Course 1", courseColor = "#FF0000"),
        ),
        recipientPickerUiState = RecipientPickerUiState(
            selectedRecipients = listOf(Recipient(name = "Person 1"), Recipient(name = "Person 2")),
        ),
        sendIndividual = true,
        subject = TextFieldValue("Test Subject"),
        body = TextFieldValue("Test Body"),
        screenState = ScreenState.Data,
        showConfirmationDialog = true,
    )
    InboxComposeScreen(
        title = "New Message",
        uiState = uiState,
    ) {}
}