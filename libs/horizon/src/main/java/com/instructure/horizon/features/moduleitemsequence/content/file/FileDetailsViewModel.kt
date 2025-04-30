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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.pandautils.features.file.download.FileDownloadWorker
import com.instructure.pandautils.room.appdatabase.daos.FileDownloadProgressDao
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressEntity
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileDetailsViewModel @Inject constructor(
    private val fileDetailsRepository: FileDetailsRepository,
    private val workManager: WorkManager,
    private val fileDownloadProgressDao: FileDownloadProgressDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val fileUrl = savedStateHandle[ModuleItemContent.File.FILE_URL] ?: ""

    private val _uiState =
        MutableStateFlow(FileDetailsUiState(url = fileUrl, onDownloadClicked = ::onDownloadClicked, onFileOpened = ::onFileOpened))

    val uiState = _uiState.asStateFlow()

    private var fileFolder: FileFolder? = null

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
        viewModelScope.tryLaunch {
            fileFolder = fileDetailsRepository.getFileFolderFromURL(fileUrl)
            val authUrl = fileDetailsRepository.getAuthenticatedFileUrl(fileUrl.replace("api/v1/", ""))
            fileFolder?.let { file ->
                _uiState.update {
                    it.copy(
                        loadingState = it.loadingState.copy(isLoading = false),
                        fileName = file.displayName.orEmpty(),
                        filePreview = getFilePreview(file, authUrl),
                        mimeType = file.contentType ?: "*/*",
                    )
                }
            }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true)) }
        }
    }

    private fun onDownloadClicked() {
        fileFolder?.let { file ->
            _uiState.update { it.copy(downloadState = FileDownloadProgressState.STARTING) }
            val workRequest = FileDownloadWorker.createOneTimeWorkRequest(file.displayName.orEmpty(), file.url.orEmpty())
            workManager.enqueue(workRequest)
            val workerId = workRequest.id.toString()
            viewModelScope.tryLaunch {
                fileDownloadProgressDao.findByWorkerIdFlow(workerId)
                    .collect { progress ->
                        updateProgress(progress)
                    }
            } catch {
                _uiState.update {
                    it.copy(downloadState = FileDownloadProgressState.ERROR)
                }
            }
        }
    }

    private fun updateProgress(progressEntity: FileDownloadProgressEntity?) {
        _uiState.update {
            it.copy(
                downloadProgress = (progressEntity?.progress ?: 0) / 100f,
                downloadState = progressEntity?.progressState ?: FileDownloadProgressState.COMPLETED
            )
        }
        if (progressEntity?.progressState == FileDownloadProgressState.COMPLETED) {
            _uiState.update {
                it.copy(filePathToOpen = progressEntity.filePath)
            }
        }
        if (progressEntity?.progressState?.isCompleted() == true) {
            viewModelScope.launch {
                fileDownloadProgressDao.deleteByWorkerId(progressEntity.workerId)
            }
        }
    }

    private fun onFileOpened() {
        _uiState.update { it.copy(filePathToOpen = null) }
    }

    private fun getFilePreview(file: FileFolder, authUrl: String): FilePreviewUiState {
        val url = file.url.orEmpty()
        val displayName = file.displayName.orEmpty()
        val contentType = file.contentType.orEmpty()
        val thumbnailUrl = file.thumbnailUrl.orEmpty()

        return when {
            contentType == "application/pdf" -> FilePreviewUiState.Pdf(url)

            contentType.startsWith("video") || contentType.startsWith("audio") -> FilePreviewUiState.Media(
                url,
                thumbnailUrl,
                contentType,
                displayName
            )

            contentType.startsWith("image") -> FilePreviewUiState.Image(displayName, url)

            else -> FilePreviewUiState.WebView("$authUrl&preview=1")
        }
    }
}