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

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.FileUploadConfig
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.ProgressRequestUpdateListener
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HorizonInboxAttachmentPickerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileUploadManager: FileUploadManager
): ViewModel() {
    private val _filesState: MutableStateFlow<List<HorizonInboxAttachment>> = MutableStateFlow(emptyList())
    val filesState = _filesState.asStateFlow()

    fun onFileAdded(uri: Uri) {
        viewModelScope.tryLaunch {
            val file = uri.toFile(context)!!

            val fso = FileSubmitObject(
                name = file.name,
                size = file.length(),
                contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension).orEmpty(),
                fullPath = file.absolutePath,
            )
            val config = FileUploadConfig.forUser(
                fso,
                parentFolderPath = "conversation_attachments",
            )

            val attachmentState = HorizonInboxAttachment(
                id = UUID.randomUUID().toString(),
                fileName = file.name,
                fileSize = file.length(),
                filePath = file.absolutePath,
                state = HorizonInboxAttachmentState.InProgress(0f)
            )
            _filesState.update { it + attachmentState }

            val result = withContext(Dispatchers.IO) {
                fileUploadManager.uploadFile(
                    config,
                    object : ProgressRequestUpdateListener {
                        override fun onProgressUpdated(
                            progressPercent: Float,
                            length: Long
                        ): Boolean {
                            updateItem(
                                attachmentState.copy(
                                    state = HorizonInboxAttachmentState.InProgress(progressPercent)
                                )
                            )
                            return true
                        }
                    }
                )
            }

            if (result.isSuccess) {
                updateItem(
                    attachmentState.copy(
                        state = HorizonInboxAttachmentState.Success
                    )
                )
            } else {
                Log.d("AttachmentPicker", "Error uploading file: ${result.isFail}")
                updateItem(
                    attachmentState.copy(
                        state = HorizonInboxAttachmentState.Error
                    )
                )
            }
        } catch {
            Log.d("AttachmentPicker", "Error uploading file: ${it.message}")
        }
    }

    private fun updateItem(item: HorizonInboxAttachment) {
        _filesState.update { currentList ->
            currentList.map {
                if (it.id == item.id) {
                    item
                } else {
                    it
                }
            }
        }
    }
}

fun Uri.toFile(context: Context): File? {
    val inputStream = context.contentResolver.openInputStream(this)
    val tempFile = File.createTempFile("temp", ".jpg")
    return try {
        tempFile.outputStream().use { fileOut ->
            inputStream?.copyTo(fileOut)
        }
        tempFile.deleteOnExit()
        inputStream?.close()
        tempFile
    } catch (e: Exception) {
        null
    }
}