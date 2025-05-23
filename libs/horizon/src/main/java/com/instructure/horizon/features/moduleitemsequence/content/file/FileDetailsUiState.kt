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

import com.instructure.horizon.features.account.filepreview.FilePreviewUiState
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState

data class FileDetailsUiState(
    val url: String = "",
    val fileName: String = "",
    val loadingState: LoadingState = LoadingState(),
    val onDownloadClicked: () -> Unit = {},
    val downloadProgress: Float = 0f,
    val downloadState: FileDownloadProgressState = FileDownloadProgressState.COMPLETED,
    val onCancelDownloadClicked: () -> Unit = {},
    val filePathToOpen: String? = null,
    val onFileOpened: () -> Unit = {},
    val filePreview: FilePreviewUiState? = null,
    val mimeType: String = "*/*",
)