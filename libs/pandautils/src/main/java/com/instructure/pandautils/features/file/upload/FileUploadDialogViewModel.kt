/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.file.upload

import android.content.res.Resources
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.itemviewmodels.FileItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.FileUploadUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.android.synthetic.main.dialog_files_upload.*
import java.lang.IllegalArgumentException
import java.util.*
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.pow

@HiltViewModel
class FileUploadDialogViewModel @Inject constructor(
        private val fileUploadUtils: FileUploadUtils,
        private val resources: Resources
) : ViewModel() {

    val data: LiveData<FileUploadDialogViewData>
        get() = _data
    private val _data = MutableLiveData<FileUploadDialogViewData>()

    val events: LiveData<Event<FileUploadAction>>
        get() = _events
    private val _events = MutableLiveData<Event<FileUploadAction>>()

    private var assignment: Assignment? = null
    private var submitObjects: MutableList<FileSubmitObject> = mutableListOf()
    private var uploadType: FileUploadType = FileUploadType.ASSIGNMENT
    private var canvasContext: CanvasContext = CanvasContext.defaultCanvasContext()

    fun setData(assignment: Assignment?, file: Uri?, uploadType: FileUploadType, canvasContext: CanvasContext) {
        this.assignment = assignment
        val submitObject = file?.let {
            getUriContents(it)
        }
        this.submitObjects = submitObject?.let { mutableListOf(it) } ?: mutableListOf()
        this.uploadType = uploadType
        this.canvasContext = canvasContext
        updateItems()
    }

    fun onCameraClicked() {
        _events.postValue(Event(FileUploadAction.TakePhoto))
    }

    fun onGalleryClicked() {
        _events.postValue(Event(FileUploadAction.PickPhoto))
    }

    fun onFilesClicked() {
        _events.postValue(Event(FileUploadAction.PickFile))
    }

    fun addFile(fileUri: Uri) {
        val submitObject = getUriContents(fileUri)
        submitObject?.let {
            if (it.errorMessage.isNullOrEmpty()) {
                val added = addIfExtensionAllowed(it)
                if (added) {
                    updateItems()
                }
            } else {
                TODO("Handle error")
            }
        } ?: TODO("Handle error")
    }

    private fun updateItems() {
        val itemViewModels = submitObjects.map {
            FileItemViewModel(FileItemViewData(
                    it.name,
                    humanReadableByteCount(it.size),
                    it.fullPath
            ), this::onRemoveFileClicked)
        }
        _data.postValue(FileUploadDialogViewData(itemViewModels))
    }

    private fun onRemoveFileClicked(fullPath: String) {
        submitObjects.removeAt(submitObjects.indexOfFirst { it.fullPath == fullPath })
        updateItems()
    }

    private fun getUriContents(fileUri: Uri): FileSubmitObject? {
        val mimeType = fileUploadUtils.getFileMimeType(fileUri)
        val fileName = fileUploadUtils.getFileNameWithDefault(fileUri)

        return fileUploadUtils.getFileSubmitObjectFromInputStream(fileUri, fileName, mimeType)
    }

    private fun addIfExtensionAllowed(fileSubmitObject: FileSubmitObject): Boolean {
        if (assignment != null && (assignment?.allowedExtensions == null || assignment?.allowedExtensions?.size == 0)) {
            submitObjects.add(fileSubmitObject)
            return true
        }

        //get the extension and compare it to the list of allowed extensions
        val index = fileSubmitObject.fullPath.lastIndexOf(".")
        if (assignment != null && index != -1) {
            val ext = fileSubmitObject.fullPath.substring(index + 1)
            for (i in 0 until (assignment?.allowedExtensions?.size ?: 0)) {
                if (assignment!!.allowedExtensions[i].trim { it <= ' ' }.equals(ext, ignoreCase = true)) {
                    submitObjects.add(fileSubmitObject)
                    return true
                }
            }
            TODO("error toast")
            return false
        }

        //if we're sharing it from an external source we won't know which assignment they're trying to
        //submit to, so we won't know if there are any extension limits
        //also, the assignment and/or course could be null due to memory pressures
        if (assignment == null || canvasContext.id != 0L) {
            submitObjects.add(fileSubmitObject)
            return true
        }

        TODO("error toast")
        return false
    }

    private fun checkIfFileSubmissionAllowed(): Boolean {
        return assignment?.submissionTypesRaw?.contains(Assignment.SubmissionType.ONLINE_UPLOAD.apiString) ?: false
    }

    private fun humanReadableByteCount(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1].toString()
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }

    private fun setupAllowedExtensions(): String? {
        return if (uploadType != FileUploadType.SUBMISSION_COMMENT && assignment != null && assignment?.allowedExtensions != null && (assignment?.allowedExtensions?.size
                        ?: 0) > 0) {
            assignment?.let {
                var extensions = resources.getString(R.string.allowedExtensions)

                for (i in 0 until it.allowedExtensions.size) {
                    extensions += it.allowedExtensions[i]
                    if (it.allowedExtensions.size > 1 && i < it.allowedExtensions.size - 1) {
                        extensions += ","
                    }
                }
                extensions
            }
        } else {
            null
        }
    }
}