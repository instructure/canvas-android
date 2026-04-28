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

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.domain.usecase.GetFileDetailsUseCase
import com.instructure.horizon.features.account.filepreview.FilePreviewUiState
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.pandautils.features.file.download.FileDownloadWorker
import com.instructure.pandautils.room.appdatabase.daos.FileDownloadProgressDao
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressEntity
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState
import com.instructure.horizon.database.dao.HorizonLocalFileDao
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.filecache.FileCache
import com.instructure.pandautils.utils.filecache.awaitFileDownload
import android.os.Environment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FileDetailsViewModel @Inject constructor(
    private val getFileDetailsUseCase: GetFileDetailsUseCase,
    private val oAuthApi: OAuthAPI.OAuthInterface,
    private val workManager: WorkManager,
    private val fileDownloadProgressDao: FileDownloadProgressDao,
    private val fileCache: FileCache,
    private val crashlytics: FirebaseCrashlytics,
    private val networkStateProvider: NetworkStateProvider,
    private val localFileDao: HorizonLocalFileDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val fileUrl = savedStateHandle[ModuleItemContent.File.FILE_URL] ?: ""
    private val courseId: Long = savedStateHandle[Const.COURSE_ID] ?: -1L

    private val _uiState =
        MutableStateFlow(
            FileDetailsUiState(
                url = fileUrl,
                onDownloadClicked = ::onDownloadClicked,
                onFileOpened = ::onFileOpened,
                onCancelDownloadClicked = ::cancelDownload
            )
        )

    val uiState = _uiState.asStateFlow()

    private var runningWorkerId: UUID? = null

    private var fileUrl_: String = ""
    private var displayName_: String = ""

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
        viewModelScope.tryLaunch {
            val fileDetails = getFileDetailsUseCase(GetFileDetailsUseCase.Params(fileUrl, courseId))
            fileUrl_ = fileDetails.url
            displayName_ = fileDetails.displayName
            val authUrl = if (fileDetails.localPath == null) {
                oAuthApi.getAuthenticatedSession(
                    "${fileDetails.url.replace("api/v1/", "")}?display=borderless",
                    RestParams(isForceReadFromNetwork = true)
                ).dataOrNull?.sessionUrl
            } else {
                null
            }
            _uiState.update {
                it.copy(
                    fileId = fileDetails.id,
                    loadingState = it.loadingState.copy(isLoading = false),
                    fileName = fileDetails.displayName,
                    filePreview = getFilePreview(
                        url = fileDetails.url,
                        displayName = fileDetails.displayName,
                        contentType = fileDetails.contentType.orEmpty(),
                        thumbnailUrl = fileDetails.thumbnailUrl.orEmpty(),
                        authUrl = authUrl,
                        localPath = fileDetails.localPath,
                    ),
                    mimeType = fileDetails.contentType ?: "*/*",
                )
            }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true)) }
        }
    }

    private fun onDownloadClicked() {
        if (!networkStateProvider.isOnline()) {
            onDownloadOffline()
        } else {
            onDownloadOnline()
        }
    }

    private fun onDownloadOffline() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(downloadState = FileDownloadProgressState.STARTING) }
            val localFile = localFileDao.findById(_uiState.value.fileId)
            if (localFile != null) {
                val srcFile = File(localFile.path)
                if (srcFile.exists()) {
                    withContext(Dispatchers.IO) {
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val destFile = File(downloadsDir, displayName_)
                        srcFile.copyTo(destFile, overwrite = true)
                    }
                    _uiState.update {
                        it.copy(
                            downloadProgress = 1f,
                            downloadState = FileDownloadProgressState.COMPLETED,
                            filePathToOpen = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), displayName_).absolutePath,
                        )
                    }
                } else {
                    _uiState.update { it.copy(downloadState = FileDownloadProgressState.ERROR) }
                }
            } else {
                _uiState.update { it.copy(downloadState = FileDownloadProgressState.ERROR) }
            }
        } catch {
            _uiState.update { it.copy(downloadState = FileDownloadProgressState.ERROR) }
        }
    }

    private fun onDownloadOnline() {
        _uiState.update { it.copy(downloadState = FileDownloadProgressState.STARTING) }
        val workRequest = FileDownloadWorker.createOneTimeWorkRequest(displayName_, fileUrl_)
        workManager.enqueue(workRequest)
        runningWorkerId = workRequest.id
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

    private suspend fun getFilePreview(
        url: String,
        displayName: String,
        contentType: String,
        thumbnailUrl: String,
        authUrl: String?,
        localPath: String?,
    ): FilePreviewUiState {
        try {
            return when {
                contentType == "application/pdf" -> {
                    val uri = if (localPath != null) {
                        Uri.fromFile(File(localPath))
                    } else {
                        Uri.fromFile(fileCache.awaitFileDownload(url) ?: return FilePreviewUiState.NoPreview)
                    }
                    FilePreviewUiState.Pdf(uri)
                }

                contentType.startsWith("video") || contentType.startsWith("audio") -> {
                    val uri = if (localPath != null) {
                        Uri.fromFile(File(localPath))
                    } else {
                        Uri.fromFile(fileCache.awaitFileDownload(url) ?: return FilePreviewUiState.NoPreview)
                    }
                    FilePreviewUiState.Media(uri, thumbnailUrl, contentType, displayName)
                }

                contentType.startsWith("image") -> {
                    val uri = if (localPath != null) {
                        Uri.fromFile(File(localPath))
                    } else {
                        Uri.fromFile(fileCache.awaitFileDownload(url) ?: return FilePreviewUiState.NoPreview)
                    }
                    FilePreviewUiState.Image(displayName = displayName, uri = uri)
                }

                else -> if (authUrl != null) {
                    FilePreviewUiState.WebView("$authUrl&preview=1")
                } else {
                    FilePreviewUiState.NoPreview
                }
            }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            return FilePreviewUiState.NoPreview
        }
    }

    private fun cancelDownload() {
        runningWorkerId?.let {
            workManager.cancelWorkById(it)
            runningWorkerId = null
            viewModelScope.tryLaunch {
                fileDownloadProgressDao.deleteByWorkerId(it.toString())
            } catch {}
        }
    }
}
