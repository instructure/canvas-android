/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.teacher.features.files.details

import android.content.res.Resources
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.backgroundColor
import com.instructure.teacher.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FileDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resources: Resources,
    private val fileDetailsRepository: FileDetailsRepository,
    private val mimeTypeMap: MimeTypeMap
) : ViewModel() {

    val canvasContext = savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT)!!
    private val fileUrl = savedStateHandle.get<String>(Const.FILE_URL).orEmpty()

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<FileDetailsViewData>
        get() = _data
    private val _data = MutableLiveData<FileDetailsViewData>()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _state.postValue(ViewState.Loading)

            val file = fileDetailsRepository.getFileFolderFromURL(fileUrl)

            val features = fileDetailsRepository.getCourseFeatures(canvasContext.id)

            val requiresUsageRights = features.contains("usage_rights_required")
            val licences = if (requiresUsageRights) {
                fileDetailsRepository.getCourseFileLicences(canvasContext.id)
            } else {
                emptyList()
            }

            val editableFile = EditableFile(
                file = file,
                usageRights = requiresUsageRights,
                licenses = licences,
                courseColor = canvasContext.backgroundColor,
                canvasContext = canvasContext,
                iconRes = R.drawable.ic_document
            )

            val extension = file.name.orEmpty().substringAfterLast('.')

            val fileViewData = if (file.contentType == "multipart/form-data") {
                val type = mimeTypeMap.getMimeTypeFromExtension(extension)
                getFileViewData(file.copy(contentType = type), extension, editableFile)
            } else {
                getFileViewData(file, extension, editableFile)
            }

            _data.postValue(FileDetailsViewData(fileViewData))
            _state.postValue(ViewState.Success)
        } catch {
            _state.postValue(ViewState.Error(resources.getString(R.string.errorOccurred)))
        }
    }

    private fun getFileViewData(file: FileFolder, extension: String, editableFile: EditableFile): FileViewData {
        val url = file.url.orEmpty()
        val displayName = file.displayName.orEmpty()
        val contentType = file.contentType.orEmpty()
        val thumbnailUrl = file.thumbnailUrl.orEmpty()

        return when {
            contentType == "application/pdf" -> FileViewData.Pdf(
                url,
                editableFile
            )

            contentType.startsWith("video") || contentType.startsWith("audio") -> FileViewData.Media(
                url,
                thumbnailUrl,
                contentType,
                displayName,
                editableFile
            )

            contentType.startsWith("image") -> FileViewData.Image(
                displayName,
                url,
                contentType,
                editableFile
            )

            contentType == "text/html" || extension == "htm" || extension == "html" -> FileViewData.Html(
                url,
                displayName,
                editableFile
            )

            else -> FileViewData.Other(
                file.url.orEmpty(),
                file.displayName.orEmpty(),
                file.contentType.orEmpty(),
                file.thumbnailUrl.orEmpty(),
                editableFile
            )
        }
    }
}
