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

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.account.filepreview.FilePreviewUiState
import com.instructure.horizon.features.moduleitemsequence.content.assignment.FileItem
import com.instructure.pandautils.features.file.download.FileDownloadWorker
import com.instructure.pandautils.room.appdatabase.daos.FileDownloadProgressDao
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressEntity
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState
import com.instructure.pandautils.utils.filecache.FileCache
import com.instructure.pandautils.utils.filecache.awaitFileDownload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FileSubmissionContentViewModel @Inject constructor(
    private val workManager: WorkManager,
    private val fileDownloadProgressDao: FileDownloadProgressDao,
    private val fileCache: FileCache
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(FileSubmissionContentUiState(onFileOpened = ::onFileOpened))

    val uiState = _uiState.asStateFlow()

    private var fileIdToWorkerIdMap = mutableMapOf<Long, String>()

    fun setInitialData(files: List<FileItem>) {
        val fileItems = files.mapIndexed { index, fileItem ->
            FileItemUiState(
                fileName = fileItem.fileName,
                fileUrl = fileItem.fileUrl,
                fileType = fileItem.fileType,
                selected = index == 0,
                fileId = fileItem.fileId,
                thumbnailUrl = fileItem.thumbnailUrl,
                onClick = { onFileClick(index) },
                onDownloadClick = { file -> downloadFile(file) },
                onCancelDownloadClick = { id ->
                    cancelDownload(id)
                }
            )
        }

        _uiState.update {
            it.copy(files = fileItems)
        }

        fileSelected(fileItems.first())
    }

    private fun downloadFile(fileItem: FileItemUiState) {
        updateFileItem(fileItem.copy(downloadState = FileDownloadProgressState.STARTING, downloadProgress = 0f))
        val workRequest = FileDownloadWorker.createOneTimeWorkRequest(fileItem.fileName, fileItem.fileUrl)
        workManager.enqueue(workRequest)
        val workerId = workRequest.id.toString()
        fileIdToWorkerIdMap[fileItem.fileId] = workerId
        viewModelScope.tryLaunch {
            fileDownloadProgressDao.findByWorkerIdFlow(workerId)
                .collect { progress ->
                    updateProgress(fileItem, progress)
                }
        } catch {
            updateFileItem(fileItem.copy(downloadState = FileDownloadProgressState.COMPLETED, downloadProgress = 0f))
        }
    }

    private fun updateProgress(fileItem: FileItemUiState, progressEntity: FileDownloadProgressEntity?) {
        updateFileItem(
            fileItem.copy(
                downloadState = progressEntity?.progressState ?: FileDownloadProgressState.COMPLETED,
                downloadProgress = (progressEntity?.progress ?: 0) / 100f
            )
        )
        if (progressEntity?.progressState == FileDownloadProgressState.COMPLETED) {
            _uiState.update {
                it.copy(filePathToOpen = progressEntity.filePath, mimeTypeToOpen = fileItem.fileType)
            }
        }
        if (progressEntity?.progressState?.isCompleted() == true) {
            viewModelScope.launch {
                fileDownloadProgressDao.deleteByWorkerId(progressEntity.workerId)
            }
        }
    }

    private fun updateFileItem(fileItem: FileItemUiState) {
        _uiState.update {
            it.copy(
                files = it.files.map { item ->
                    if (item.fileId == fileItem.fileId) {
                        fileItem
                    } else {
                        item
                    }
                }
            )
        }
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
            _uiState.update { it.copy(filePreviewLoading = true) }
            val filePreview = getFilePreview(fileItem)
            _uiState.update {
                it.copy(filePreview = filePreview, filePreviewLoading = false)
            }
        } catch {
            _uiState.update {
                it.copy(filePreview = FilePreviewUiState.NoPreview, filePreviewLoading = false)
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

            contentType.startsWith("video") || contentType.startsWith("audio") -> {
                val tempFile: File? = fileCache.awaitFileDownload(file.fileUrl)
                tempFile?.let {
                    FilePreviewUiState.Media(
                        Uri.fromFile(it),
                        thumbnailUrl,
                        contentType,
                        displayName
                    )
                } ?: FilePreviewUiState.NoPreview
            }

            contentType.startsWith("image") -> {
                val tempFile: File? = fileCache.awaitFileDownload(file.fileUrl)
                tempFile?.let {
                    FilePreviewUiState.Image(
                        displayName = displayName,
                        uri = Uri.fromFile(it)
                    )
                } ?: FilePreviewUiState.NoPreview
            }

            contentType.startsWith("text") -> {
                val tempFile: File? = fileCache.awaitFileDownload(file.fileUrl)
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

    private fun onFileOpened() {
        _uiState.update { it.copy(filePathToOpen = null, mimeTypeToOpen = null) }
    }

    private fun cancelDownload(fileId: Long) {
        val workerId = fileIdToWorkerIdMap[fileId]
        if (workerId != null) {
            workManager.cancelWorkById(UUID.fromString(workerId))
            fileIdToWorkerIdMap.remove(fileId)
            viewModelScope.tryLaunch {
                fileDownloadProgressDao.deleteByWorkerId(workerId)
            } catch {}
        }
    }
}