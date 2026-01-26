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
package com.instructure.horizon.features.moduleitemsequence.content.file

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.account.filepreview.FilePreview
import com.instructure.horizon.features.aiassistant.common.model.AiAssistContextSource
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItem
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItemState
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState.ERROR
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState.IN_PROGRESS
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState.STARTING
import com.instructure.pandautils.utils.openFile

@Composable
fun FileDetailsContentScreen(
    uiState: FileDetailsUiState,
    updateAiContext: (AiAssistContextSource, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    LaunchedEffect(uiState.filePathToOpen) {
        if (uiState.filePathToOpen != null) {
            context.openFile(uiState.filePathToOpen, uiState.mimeType, context.getString(R.string.fileDetails_openWith))
            uiState.onFileOpened()
        }
    }

    LaunchedEffect(uiState.fileId) {
        updateAiContext(
            AiAssistContextSource.File(uiState.fileId.toString()),
            ""
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(HorizonCornerRadius.level5)
            .background(HorizonColors.Surface.cardPrimary()), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.loadingState.isLoading) {
            Spinner(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            AnimatedContent(
                targetState = uiState.downloadState == IN_PROGRESS || uiState.downloadState == STARTING,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "DownloadButtonOrProgressAnimation"
            ) { isRunning ->
                if (isRunning) {
                    FileDropItem(
                        FileDropItemState.InProgress(
                            uiState.fileName,
                            if (uiState.downloadState == IN_PROGRESS) uiState.downloadProgress else null,
                            uiState.onCancelDownloadClicked
                        ),
                        hasBorder = false,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uiState.downloadState == ERROR) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp, alignment = Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(R.drawable.error),
                                    tint = HorizonColors.Icon.error(),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = stringResource(R.string.fileDetails_downloadError),
                                    style = HorizonTypography.p1,
                                    color = HorizonColors.Text.error()
                                )
                            }
                            HorizonSpace(SpaceSize.SPACE_16)
                        }
                        Button(
                            label = stringResource(R.string.fileDetails_downloadFile),
                            height = ButtonHeight.SMALL,
                            color = ButtonColor.Institution,
                            iconPosition = ButtonIconPosition.End(R.drawable.download),
                            onClick = uiState.onDownloadClicked
                        )
                    }
                }
            }
            uiState.filePreview?.let {
                FilePreview(it)
            }
        }
    }
}

@Preview
@Composable
fun FileDetailsContentScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    FileDetailsContentScreen(
        uiState = FileDetailsUiState(
            url = "https://example.com/file.pdf"
        ),
        { _, _ -> },
        modifier = Modifier.fillMaxSize()
    )
}

@Preview
@Composable
fun FileDetailsContentScreenErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    FileDetailsContentScreen(
        uiState = FileDetailsUiState(
            url = "https://example.com/file.pdf",
            downloadState = ERROR
        ),
        { _, _ -> },
        modifier = Modifier.fillMaxSize()
    )
}