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
package com.instructure.horizon.features.inbox.attachment

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.FileUploadConfig
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.utils.ProgressRequestUpdateListener
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.features.file.upload.FileUploadUtilsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HorizonInboxAttachmentPickerViewModel @Inject constructor(
    private val fileUploadManager: FileUploadManager,
    private val fileUploadUtils: FileUploadUtilsHelper
): ViewModel() {
    private val _uiState: MutableStateFlow<HorizonInboxAttachmentPickerUiState> = MutableStateFlow(
        HorizonInboxAttachmentPickerUiState(
            onFileSelected = ::onFileAdded
        )
    )
    val uiState = _uiState.asStateFlow()

    private fun onFileAdded(uri: Uri) {
        viewModelScope.tryLaunch {
            val fso = fileUploadUtils.getFileSubmitObjectFromInputStream(
                uri,
                fileUploadUtils.getFileNameWithDefault(uri),
                fileUploadUtils.getFileMimeType(uri)
            )!!

            val config = FileUploadConfig.forUser(
                fso,
                parentFolderPath = "conversation attachments",
            )

            val attachmentState = HorizonInboxAttachment(
                id = Random.nextLong(),
                fileName = fso.name,
                fileSize = fso.size,
                filePath = fso.fullPath,
                state = HorizonInboxAttachmentState.InProgress(0f)
            )
            _uiState.update { it.copy(files = it.files + attachmentState) }

            val result = withContext(Dispatchers.IO) {
                fileUploadManager.uploadFile(
                    config,
                    object : ProgressRequestUpdateListener {
                        override fun onProgressUpdated(
                            progressPercent: Float,
                            length: Long
                        ): Boolean {
                            updateItem(
                                attachmentState.id,
                                attachmentState.copy(
                                    state = HorizonInboxAttachmentState.InProgress(progressPercent / 100)
                                )
                            )
                            return true
                        }
                    },
                    RestParams(shouldIgnoreToken = true, disableFileVerifiers = false)
                )
            }

            if (result.isSuccess) {
                val id = result.dataOrNull?.id ?: attachmentState.id
                updateItem(
                    attachmentState.id,
                    attachmentState.copy(
                        id = id,
                        state = HorizonInboxAttachmentState.Success,
                        onActionClicked = {
                            removeItem(id)
                        }
                    )
                )
            } else {
                updateItem(
                    attachmentState.id,
                    attachmentState.copy(
                        state = HorizonInboxAttachmentState.Error,
                        onActionClicked = {
                            removeItem(attachmentState.id)
                        }
                    )
                )
            }
        } catch { }
    }

    private fun updateItem(id: Long, item: HorizonInboxAttachment) {
        _uiState.update {
            it.copy(
                files = it.files.map { file ->
                    if (file.id == id) {
                        item
                    } else {
                        file
                    }
                }
            )
        }
    }

    private fun removeItem(id: Long) {
        _uiState.update {
            it.copy(
                files = it.files.filterNot { file -> file.id == id }
            )
        }
    }
}