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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandares.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.LabelSwitchRow
import com.instructure.pandautils.compose.composables.LabelTextFieldRow
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.MultipleValuesRow
import com.instructure.pandautils.compose.composables.MultipleValuesRowAction
import com.instructure.pandautils.compose.composables.SelectContextUiState
import com.instructure.pandautils.compose.composables.SimpleAlertDialog
import com.instructure.pandautils.compose.composables.TextFieldWithHeader
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.pandautils.features.inbox.compose.InboxComposeActionHandler
import com.instructure.pandautils.features.inbox.compose.InboxComposeUiState
import com.instructure.pandautils.features.inbox.compose.RecipientPickerUiState
import com.instructure.pandautils.features.inbox.utils.AttachmentCard
import com.instructure.pandautils.features.inbox.utils.AttachmentCardItem
import com.instructure.pandautils.features.inbox.utils.AttachmentStatus
import com.instructure.pandautils.utils.ScreenState
import com.instructure.pandautils.utils.handleUrlAt
import com.instructure.pandautils.utils.linkify

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
                    navigationActionClick = { actionHandler(InboxComposeActionHandler.Close) },
                    actions = {
                        if (uiState.screenState == ScreenState.Loading) {
                            Loading(
                                modifier = Modifier.testTag("Loading"),
                            )
                        } else {
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
                        }
                    },
                )
            },
            content = { padding ->
                Column(
                   modifier = Modifier.fillMaxSize()
                ) {
                    InboxComposeScreenContent(padding, subjectFocusRequester, bodyFocusRequester, uiState, actionHandler)
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
private fun InboxComposeScreenContent(
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
            .testTag("InboxComposeScreenContent")
    ) {
        if (uiState.hiddenFields.isContextHidden.not()) {
            ContextValueRow(
                label = stringResource(id = R.string.course),
                value = uiState.selectContextUiState.selectedCanvasContext,
                enabled = uiState.disabledFields.isContextDisabled.not(),
                onClick = { actionHandler(InboxComposeActionHandler.OpenContextPicker) },
            )
        }

        CanvasDivider()

        AnimatedVisibility(visible = uiState.selectContextUiState.selectedCanvasContext != null && uiState.hiddenFields.isContextHidden.not()) {
            Column {
                val context = LocalContext.current
                val view = LocalView.current
                MultipleValuesRow(
                    label = stringResource(R.string.recipientsTo),
                    uiState = uiState.inlineRecipientSelectorState,
                    itemComposable = { recipient, enabled ->
                        RecipientChip(enabled, recipient) {
                            actionHandler(InboxComposeActionHandler.RemoveRecipient(recipient))
                            view.announceForAccessibility(
                                context.getString(
                                    com.instructure.pandautils.R.string.a11y_recipientIsRemoved,
                                    recipient.name
                                ))
                        }
                    },
                    actionHandler = { action ->
                        when (action) {
                            is MultipleValuesRowAction.AddValueClicked -> actionHandler(
                                InboxComposeActionHandler.OpenRecipientPicker
                            )

                            is MultipleValuesRowAction.SearchValueSelected<*> -> {
                                (action.value as? Recipient)?.let {
                                    actionHandler(
                                        InboxComposeActionHandler.AddRecipient(it)
                                    )
                                }
                            }

                            is MultipleValuesRowAction.SearchQueryChanges -> actionHandler(
                                InboxComposeActionHandler.SearchRecipientQueryChanged(action.searchQuery)
                            )

                            is MultipleValuesRowAction.HideSearchResults -> actionHandler(
                                InboxComposeActionHandler.HideSearchResults
                            )
                        }
                    },
                    searchResultComposable = { recipient ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            UserAvatar(
                                imageUrl = recipient.avatarURL,
                                name = recipient.name ?: "",
                                modifier = Modifier
                                    .size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                recipient.name ?: "",
                                color = colorResource(id = R.color.textDarkest),
                            )
                        }
                    }
                )

                CanvasDivider()
            }
        }

        if (uiState.hiddenFields.isSendIndividualHidden.not()) {
            LabelSwitchRow(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.sendIndividualMessage),
                subtitle =
                    if (uiState.isSendIndividualMandatory)
                        stringResource(R.string.sendIndividualMessageIsMandatory)
                    else
                        null,
                checked = uiState.isSendIndividualEnabled,
                onCheckedChange = {
                    actionHandler(InboxComposeActionHandler.SendIndividualChanged(it))
                },
                enabled = uiState.disabledFields.isSendIndividualDisabled.not() && uiState.isSendIndividualMandatory.not()
            )

            CanvasDivider()
        }

        if (uiState.hiddenFields.isSubjectHidden.not()) {
            LabelTextFieldRow(
                value = uiState.subject,
                label = stringResource(R.string.subject),
                onValueChange = {
                    actionHandler(InboxComposeActionHandler.SubjectChanged(it))
                },
                enabled = uiState.disabledFields.isSubjectDisabled.not(),
                focusRequester = subjectFocusRequester,
            )

            CanvasDivider()
        }

        if (uiState.hiddenFields.isBodyHidden.not()) {
            TextFieldWithHeader(
                label = stringResource(R.string.message),
                value = uiState.body,
                enabled = uiState.disabledFields.isBodyDisabled.not(),
                headerEnabled = uiState.disabledFields.isAttachmentDisabled.not(),
                headerIconResource = if (uiState.hiddenFields.isBodyHidden) null else R.drawable.ic_attachment,
                iconContentDescription = if (uiState.hiddenFields.isBodyHidden) null else stringResource(id = R.string.a11y_addAttachment),
                onValueChange = {
                    actionHandler(InboxComposeActionHandler.BodyChanged(it))
                },
                onIconClick = {
                    actionHandler(InboxComposeActionHandler.AddAttachmentSelected)
                },
                focusRequester = bodyFocusRequester,
                modifier = Modifier
                    .defaultMinSize(minHeight = 100.dp)
            )
            if (uiState.signatureLoading) {
                Loading(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                        .testTag("SignatureLoading"),
                )
            }
        }

        Column {
            uiState.attachments.forEach { attachment ->
                AttachmentCard(
                    attachmentCardItem = attachment,
                    onSelect = { actionHandler(InboxComposeActionHandler.OpenAttachment(attachment)) },
                    onRemove = { actionHandler(InboxComposeActionHandler.RemoveAttachment(attachment)) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp)
                )
            }
        }

        uiState.previousMessages?.let { previousMessages ->
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .animateContentSize()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    stringResource(com.instructure.pandautils.R.string.previousMessages),
                    color = colorResource(id = R.color.textDarkest),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(16.dp))

                CanvasDivider()

                Spacer(modifier = Modifier.height(16.dp))

                previousMessages.previousMessages.forEach { message ->
                    PreviousMessageView(previousMessages.conversation, message, actionHandler)

                    Spacer(modifier = Modifier.height(16.dp))

                    CanvasDivider()

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PreviousMessageView(
    conversation: Conversation,
    message: Message,
    actionHandler: (InboxComposeActionHandler) -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val rotationAnimation by animateFloatAsState(
        targetValue = if (isExpanded) 180F else 0F,
        animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing),
        label = "messageExpandIconRotation"

    )

    Column(
        modifier = Modifier
            .animateContentSize()
            .testTag("previousMessageView")
    ) {
        Column(
            modifier = Modifier
                .clickable {
                    isExpanded = !isExpanded
                }
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = conversation.participants.firstOrNull { it.id == message.authorId }?.name
                        ?: "",
                    color = colorResource(id = R.color.textDarkest),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = DateHelper.getFormattedDate(LocalContext.current, DateHelper.stringToDateWithMillis(message.createdAt)) ?: "",
                    color = colorResource(id = R.color.textDark),
                    fontSize = 12.sp,
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    contentDescription = null,
                    tint = colorResource(id = R.color.textDark),
                    modifier = Modifier
                        .rotate(rotationAnimation)
                )
            }
            val annotatedBody = message.body?.linkify(
                SpanStyle(
                    color = colorResource(id = com.instructure.pandautils.R.color.textInfo),
                    textDecoration = TextDecoration.Underline
                )
            ) ?: AnnotatedString("")

            SelectionContainer {
                ClickableText(
                    text = annotatedBody,
                    onClick = {
                        annotatedBody.handleUrlAt(it) {
                            actionHandler(InboxComposeActionHandler.UrlSelected(it))
                        }
                    },
                    style = TextStyle.Default.copy(
                        color = colorResource(id = R.color.textDark),
                        fontSize = 14.sp,
                    ),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (message.attachments.isNotEmpty() && isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))

            message.attachments
                .map { AttachmentCardItem(it, AttachmentStatus.UPLOADED, true) }
                .forEach { attachment ->
                    AttachmentCard(
                        attachmentCardItem = attachment,
                        onSelect = { actionHandler(InboxComposeActionHandler.OpenAttachment(attachment)) },
                        onRemove = {},
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
        }
    }
}

@Preview
@Composable
fun InboxComposeScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val uiState = InboxComposeUiState(
        selectContextUiState = SelectContextUiState(
            selectedCanvasContext = Course(id = 1, name = "Course 1", courseColor = "#FF0000"),
        ),
        recipientPickerUiState = RecipientPickerUiState(
            selectedRecipients = listOf(Recipient(stringId = "1", name = "Person 1"), Recipient(stringId = "2", name = "Person 2")),
        ),
        sendIndividual = true,
        subject = TextFieldValue("Test Subject"),
        body = TextFieldValue("Test Body"),
        screenState = ScreenState.Content,
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
        selectContextUiState = SelectContextUiState(
            selectedCanvasContext = Course(id = 1, name = "Course 1", courseColor = "#FF0000"),
        ),
        recipientPickerUiState = RecipientPickerUiState(
            selectedRecipients = listOf(Recipient(name = "Person 1"), Recipient(name = "Person 2")),
        ),
        sendIndividual = true,
        subject = TextFieldValue("Test Subject"),
        body = TextFieldValue("Test Body"),
        screenState = ScreenState.Content,
        showConfirmationDialog = true,
    )
    InboxComposeScreen(
        title = "New Message",
        uiState = uiState,
    ) {}
}