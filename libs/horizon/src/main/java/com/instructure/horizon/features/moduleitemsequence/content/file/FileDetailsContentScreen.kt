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

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItem
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItemState
import com.instructure.pandautils.compose.composables.filedetails.ImageFileContent
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState
import com.instructure.pandautils.utils.Const
import java.io.File

@Composable
fun FileDetailsContentScreen(
    uiState: FileDetailsUiState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    LaunchedEffect(uiState.filePathToOpen) {
        if (uiState.filePathToOpen != null) {
            openFile(uiState.filePathToOpen, context)
            uiState.onFileOpened()
        }
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
                targetState = uiState.downloadState.isRunning(),
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "DownloadButtonOrProgressAnimation"
            ) { isRunning ->
                if (isRunning) {
                    FileDropItem(
                        FileDropItemState.InProgress(
                            uiState.fileName,
                            if (uiState.downloadState == FileDownloadProgressState.IN_PROGRESS) uiState.downloadProgress else null,
                            uiState.onCancelDownloadClicked
                        ),
                        hasBorder = false,
                        modifier = modifier.padding(vertical = 4.dp)
                    )
                } else {
                    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Button(
                            label = stringResource(R.string.fileDetails_downloadFile),
                            height = ButtonHeight.SMALL,
                            color = ButtonColor.Institution,
                            iconPosition = ButtonIconPosition.End(R.drawable.download),
                            modifier = modifier.padding(vertical = 20.dp, horizontal = 24.dp),
                            onClick = uiState.onDownloadClicked
                        )
                    }
                }
            }
            FilePreview(uiState.filePreview)
        }
    }
}

private fun openFile(
    filePathToOpen: String,
    context: Context
) {
    val file = File(filePathToOpen)
    val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + Const.FILE_PROVIDER_AUTHORITY, file)

    val mimeType = when (file.extension.lowercase()) {
        "pdf" -> "application/pdf"
        "txt" -> "text/plain"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        else -> "*/*"
    }

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Open with"))
}

@Composable
fun FilePreview(filePreviewUiState: FilePreviewUiState?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        when (filePreviewUiState) {
            is FilePreviewUiState.Image -> ImageFileContent(
                imageUrl = filePreviewUiState.url,
                contentDescription = filePreviewUiState.displayName
            )
            is FilePreviewUiState.Media -> {}
            is FilePreviewUiState.Pdf -> {}
            is FilePreviewUiState.Text -> {}
            else -> {}
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
        modifier = Modifier.fillMaxSize()
    )
}