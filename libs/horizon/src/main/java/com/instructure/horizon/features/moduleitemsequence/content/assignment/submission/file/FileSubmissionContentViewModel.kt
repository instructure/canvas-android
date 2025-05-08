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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.account.filepreview.FilePreviewUiState
import com.instructure.horizon.features.moduleitemsequence.content.assignment.FileItem
import com.instructure.pandautils.utils.filecache.FileCache
import com.instructure.pandautils.utils.filecache.awaitFileDownload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileSubmissionContentViewModel @Inject constructor() : ViewModel() {

    private val _uiState =
        MutableStateFlow(FileSubmissionContentUiState())

    val uiState = _uiState.asStateFlow()

    fun setInitialData(files: List<FileItem>) {
        val fileItems = files.mapIndexed { index, fileItem ->
            FileItemUiState(
                fileName = fileItem.fileName,
                fileUrl = fileItem.fileUrl,
                fileType = fileItem.fileType,
                selected = index == 0,
                fileId = fileItem.fileId,
                thumbnailUrl = "",
                onClick = { onFileClick(index) },
                onDownloadClick = { /* Handle download click */ }
            )
        }

        _uiState.update {
            it.copy(files = fileItems)
        }

        fileSelected(fileItems.first())
    }

    private fun onFileClick(index: Int) {
        _uiState.update {
            it.copy(
                files = it.files.mapIndexed { i, fileItem ->
                    fileItem.copy(selected = i == index)
                }
            )
        }

        fileSelected(_uiState.value.files[index])
    }

    private fun fileSelected(fileItem: FileItemUiState) {
        viewModelScope.tryLaunch {
            val filePreview = getFilePreview(fileItem)
                _uiState.update {
                    it.copy(filePreview = filePreview)
                }
        } catch {
            _uiState.update {
                it.copy(filePreview = FilePreviewUiState.NoPreview)
            }
        }
    }

    private suspend fun getFilePreview(file: FileItemUiState): FilePreviewUiState {
        val url = file.fileUrl
        val displayName = file.fileName
        val contentType = file.fileType
        val thumbnailUrl = file.thumbnailUrl

        return when {
            contentType == "application/pdf" -> FilePreviewUiState.Pdf(url)

            contentType.startsWith("video") || contentType.startsWith("audio") -> FilePreviewUiState.Media(
                url,
                thumbnailUrl,
                contentType,
                displayName
            )

            contentType.startsWith("image") -> FilePreviewUiState.Image(displayName, url)

            contentType.startsWith("text") -> {
                val tempFile: File? = FileCache.awaitFileDownload(file.fileUrl)
                tempFile?.let {
                    FilePreviewUiState.Text(
                        content = it.readText(),
                        contentType = contentType
                    )
                } ?: FilePreviewUiState.NoPreview
            }

            else -> {
                FilePreviewUiState.NoPreview
            }
        }
    }
}