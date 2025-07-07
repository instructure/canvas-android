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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.file

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.account.filepreview.FilePreview
import com.instructure.horizon.features.account.filepreview.FilePreviewUiState
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItem
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItemState
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState
import com.instructure.pandautils.utils.openFile

@Composable
fun FileSubmissionContent(
    uiState: FileSubmissionContentUiState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    LaunchedEffect(uiState.filePathToOpen) {
        if (uiState.filePathToOpen != null) {
            context.openFile(uiState.filePathToOpen, uiState.mimeTypeToOpen ?: "*/*", context.getString(R.string.fileDetails_openWith))
            uiState.onFileOpened()
        }
    }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.check_circle_full), contentDescription = null, tint = HorizonColors.Icon.success())
            Spacer(Modifier.size(6.dp))
            Text(text = stringResource(R.string.assignmentDetails_fileSubmission), style = HorizonTypography.h3)
        }
        HorizonSpace(SpaceSize.SPACE_16)
        Column {
            uiState.files.forEach { file ->
                val borderColor = if (file.selected) HorizonColors.Surface.institution() else HorizonColors.LineAndBorder.lineStroke()
                val fileDropItemState =
                    if (file.downloadState == FileDownloadProgressState.STARTING || file.downloadState == FileDownloadProgressState.IN_PROGRESS) {
                        FileDropItemState.InProgress(
                            fileName = file.fileName,
                            progress = file.downloadProgress,
                            onActionClick = { file.onCancelDownloadClick(file.fileId) },
                            onClick = file.onClick
                        )
                    } else {
                        FileDropItemState.NoLongerEditable(
                            fileName = file.fileName,
                            onActionClick = { file.onDownloadClick(file) },
                            onClick = file.onClick
                        )
                    }
                FileDropItem(
                    state = fileDropItemState, borderColor = borderColor
                )
            }
            HorizonSpace(SpaceSize.SPACE_8)
            if (uiState.filePreviewLoading) {
                HorizonSpace(SpaceSize.SPACE_8)
                Spinner(Modifier.fillMaxWidth())
            }
            if (uiState.filePreview != null && !uiState.filePreviewLoading) {
                FilePreview(filePreviewUiState = uiState.filePreview, modifier = Modifier.fillMaxWidth().then(
                    if (uiState.filePreview is FilePreviewUiState.Text) Modifier.fillMaxHeight() else Modifier.heightIn(0.dp, 400.dp)
                ))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FileSubmissionContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    val fileItem = FileItemUiState(
        fileName = "File Name",
        fileUrl = "https://example.com/file.pdf",
        fileType = "application/pdf",
        fileId = 123L,
        thumbnailUrl = ""
    )
    val fileItemSelected = fileItem.copy(fileName = "File 2", selected = true)
    val uiState = FileSubmissionContentUiState(
        files = listOf(fileItem, fileItemSelected),
        onFileOpened = {},
        filePathToOpen = null,
        mimeTypeToOpen = null,
        filePreview = null
    )
    FileSubmissionContent(uiState)
}