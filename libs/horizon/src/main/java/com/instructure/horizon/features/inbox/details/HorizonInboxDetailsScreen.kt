/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.inbox.details

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.inbox.attachment.HorizonInboxAttachmentPicker
import com.instructure.horizon.features.inbox.attachment.HorizonInboxAttachmentPickerViewModel
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.HorizonDivider
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.SpinnerSize
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItem
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItemState
import com.instructure.horizon.horizonui.organisms.Modal
import com.instructure.horizon.horizonui.organisms.ModalDialogState
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextArea
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextAreaState
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.pandautils.utils.localisedFormat
import com.instructure.pandautils.utils.orDefault
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizonInboxDetailsScreen(
    state: HorizonInboxDetailsUiState,
    navController: NavHostController
) {
    Scaffold(
        containerColor = HorizonColors.Surface.pagePrimary(),
        topBar = { HorizonInboxDetailsHeader(state.title, state.titleIcon, state, navController) },
    ) { innerPadding ->
        LoadingStateWrapper(state.loadingState, modifier = Modifier.padding(innerPadding)) {
            BackHandler { onExit(state, navController) }

            state.replyState?.let { replyState ->
                val viewModel: HorizonInboxAttachmentPickerViewModel = hiltViewModel()
                val pickerState by viewModel.uiState.collectAsState()
                HorizonInboxAttachmentPicker(
                    showBottomSheet = replyState.showAttachmentPicker,
                    onDismissBottomSheet = { replyState.onShowAttachmentPickerChanged(false) },
                    state = pickerState,
                    onFilesChanged = replyState.onAttachmentsChanged
                )

                if (replyState.showExitConfirmationDialog) {
                    Modal(
                        dialogState = ModalDialogState(
                            title = stringResource(R.string.exitConfirmationTitle),
                            message = stringResource(R.string.exitConfirmationMessage),
                            primaryButtonTitle = stringResource(R.string.exitConfirmationExitButtonLabel),
                            secondaryButtonTitle = stringResource(R.string.exitConfirmationCancelButtonLabel),
                            primaryButtonClick = {
                                replyState.updateShowExitConfirmationDialog(false)
                                navController.popBackStack()
                            },
                            secondaryButtonClick = { replyState.updateShowExitConfirmationDialog(false) }
                        )
                    )
                }
            }

            HorizonInboxDetailsContent(state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HorizonInboxDetailsHeader(
    title: String,
    @DrawableRes titleIcon: Int?,
    state: HorizonInboxDetailsUiState,
    navController: NavHostController
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (titleIcon != null) {
                    Icon(
                        painterResource(id = titleIcon),
                        contentDescription = null,
                        tint = HorizonColors.Icon.default(),
                        modifier = Modifier.size(16.dp)
                    )

                    HorizonSpace(SpaceSize.SPACE_4)
                }

                Text(
                    text = title,
                    style = HorizonTypography.labelLargeBold,
                    color = HorizonColors.Surface.institution(),
                    maxLines = 2
                )
            }
        },
        navigationIcon = {
            IconButton(
                iconRes = R.drawable.arrow_back,
                contentDescription = stringResource(R.string.a11yNavigateBack),
                color = IconButtonColor.Ghost,
                onClick = { onExit(state, navController) },
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HorizonColors.Surface.pagePrimary(),
            titleContentColor = HorizonColors.Text.title(),
            navigationIconContentColor = HorizonColors.Icon.default()
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HorizonInboxDetailsContent(
    state: HorizonInboxDetailsUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(HorizonCornerRadius.level4Top)
            .background(HorizonColors.Surface.pageSecondary())
    ) {
        val scrollState = rememberLazyListState(
            if (state.bottomLayout) {
                state.items.lastIndex
            } else {
                0
            }
        )
        LazyColumn(
            verticalArrangement = if (state.bottomLayout) Arrangement.Bottom else Arrangement.Top,
            state = scrollState,
            modifier = Modifier
                .weight(1f)
                .background(HorizonColors.Surface.pageSecondary())
                .semantics {
                    isTraversalGroup = true
                },
            contentPadding = PaddingValues(top = 16.dp)
        ) {
            itemsIndexed(state.items) { index, item ->
                Column {
                    HorizonInboxDetailsItem(
                        item,
                        Modifier.semantics(true) {}
                    )
                    if (item != state.items.lastOrNull()) {
                        HorizonDivider()
                    }
                }
            }
        }
        if (state.replyState != null) {
            HorizonInboxReplyContent(state.replyState)
        }
    }
}

@Composable
private fun HorizonInboxDetailsItem(
    item: HorizonInboxDetailsItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HorizonColors.Surface.pageSecondary())
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = item.author,
                style = HorizonTypography.labelLargeBold,
                color = HorizonColors.Text.body(),
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = item.date.localisedFormat("MMM d yyyy, hh mm a", LocalContext.current),
                style = HorizonTypography.p3,
                color = HorizonColors.Text.timestamp(),
            )
        }

        HorizonSpace(SpaceSize.SPACE_8)

        if (item.isHtmlContent) {
            HorizonInboxHtmlContent(item.content, Modifier.padding(horizontal = 16.dp))
        } else {
            HorizonInboxTextContent(item.content, Modifier.padding(horizontal = 24.dp))
        }

        item.attachments.forEach { attachment ->
            HorizonSpace(SpaceSize.SPACE_8)

            val fileState =
                if (attachment.downloadState == FileDownloadProgressState.STARTING || attachment.downloadState == FileDownloadProgressState.IN_PROGRESS) {
                    FileDropItemState.InProgress(
                        fileName = attachment.name,
                        progress = attachment.downloadProgress,
                        onActionClick = { attachment.onCancelDownloadClick(attachment.id) },
                    )
                } else {
                    FileDropItemState.NoLongerEditable(
                        fileName = attachment.name,
                        onActionClick = { attachment.onDownloadClick(attachment) },
                        onClick = { attachment.onDownloadClick(attachment) }
                    )
                }

            FileDropItem(
                state = fileState,
                hasBorder = true,
                borderColor = HorizonColors.LineAndBorder.lineStroke(),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun HorizonInboxTextContent(
    content: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = content,
        style = HorizonTypography.p1,
        color = HorizonColors.Text.body(),
        modifier = modifier
    )
}

@Composable
private fun HorizonInboxHtmlContent(
    content: String,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.getActivityOrNull()

    ComposeCanvasWebViewWrapper(
        content,
        modifier,
        embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
            shouldLaunchInternalWebViewFragment = { _ -> true },
            launchInternalWebViewFragment = { url -> activity?.launchCustomTab(url, ThemePrefs.brandColor) }
        ),
    )
}

@Composable
private fun HorizonInboxReplyContent(state: HorizonInboxReplyState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HorizonColors.Surface.pagePrimary())
            .padding(horizontal = 24.dp, vertical = 16.dp)

    ) {
        var isFocused by remember { mutableStateOf(false) }
        val textAreaState = TextAreaState(
            value = state.replyTextValue,
            onValueChange = state.onReplyTextValueChange,
            placeHolderText = stringResource(R.string.inboxReplyPlaceholder),
            isFocused = isFocused,
            onFocusChanged = { isFocused = it },
        )
        TextArea(textAreaState)

        HorizonSpace(SpaceSize.SPACE_16)

        state.attachments.forEach {
            FileDropItem(
                it.toFileDropItemState(),
            )
        }

        if (state.attachments.isNotEmpty()) {
            HorizonSpace(SpaceSize.SPACE_8)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            Button(
                label = stringResource(R.string.inboxDetailsReplyAttachFile),
                iconPosition = ButtonIconPosition.Start(R.drawable.attach_file),
                color = ButtonColor.Inverse,
                enabled = state.attachments.size < 3,
                onClick = { state.onShowAttachmentPickerChanged(true) },
            )

            Spacer(modifier = Modifier.weight(1f))

            AnimatedContent(
                state.isLoading,
                label = "ReplyButtonAnimation",
            ) { isLoading ->
                if (isLoading) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level6)
                    ) {
                        Spinner(
                            size = SpinnerSize.EXTRA_SMALL,
                            color = HorizonColors.Surface.cardPrimary(),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 22.dp, vertical = 10.dp),
                        )
                    }
                } else {
                    Button(
                        label = stringResource(R.string.inboxSendLabel),
                        color = ButtonColor.Institution,
                        onClick = { state.onSendReply() },
                    )
                }
            }
        }
    }
}

private fun onExit(
    state: HorizonInboxDetailsUiState,
    navController: NavHostController
) {
    if (state.replyState?.replyTextValue?.text?.isNotBlank().orDefault()) {
        state.replyState?.updateShowExitConfirmationDialog?.let { it(true) }
    } else {
        navController.popBackStack()
    }
}

@Composable
@Preview
private fun HorizonInboxDetailsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current

    HorizonInboxDetailsScreen(
        navController = rememberNavController(),
        state = HorizonInboxDetailsUiState(
            loadingState = LoadingState(),
            title = "Message",
            titleIcon = null,
            items = listOf(
                HorizonInboxDetailsItem(
                    author = "John Doe",
                    date = Date(),
                    isHtmlContent = false,
                    content = "This is a sample message content.",
                    attachments = emptyList()
                ),
                HorizonInboxDetailsItem(
                    author = "John Doe",
                    date = Date(),
                    isHtmlContent = false,
                    content = "This is a sample message content.",
                    attachments = listOf(
                        HorizonInboxDetailsAttachment(
                            id = 1L,
                            name = "Sample Attachment.pdf",
                            url = "https://www.example.com/sample.pdf",
                            contentType = "application/pdf",
                            onDownloadClick = {},
                            onCancelDownloadClick = {},
                            downloadState = FileDownloadProgressState.COMPLETED,
                            downloadProgress = 0f
                        )
                    )
                )
            ),
            replyState = HorizonInboxReplyState(
                replyTextValue = TextFieldValue(""),
                onReplyTextValueChange = {},
                onSendReply = {}
            ),
        )
    )
}