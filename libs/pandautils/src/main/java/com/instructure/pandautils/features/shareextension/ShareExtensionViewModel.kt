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

package com.instructure.pandautils.features.shareextension

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.features.file.upload.FileUploadType
import com.instructure.pandautils.mvvm.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ShareExtensionViewModel @Inject constructor(
        private val apiPrefs: ApiPrefs,
        private val resources: Resources
) : ViewModel() {

    var uri: Uri? = null
    var uploadType = FileUploadType.USER

    val events: LiveData<Event<ShareExtensionAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ShareExtensionAction>>()

    fun checkIfLoggedIn(): Boolean {
        return apiPrefs.getValidToken().isNotEmpty()
    }

    fun parseIntentType(intent: Intent) {
        val action = intent.action
        val type = intent.type

        uri = if (Intent.ACTION_SEND == action && type != null) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        } else {
            _events.postValue(Event(ShareExtensionAction.ShowToast(resources.getString(R.string.uploadingFromSourceFailed))))
            null
        }
    }

    fun showUploadDialog(course: CanvasContext?, assignment: Assignment?, uploadType: FileUploadType) {
        this.uploadType = uploadType
        uri?.let {
            when (uploadType) {
                FileUploadType.USER -> _events.postValue(Event(ShareExtensionAction.ShowMyFilesUploadDialog(it, this::uploadDialogCallback, this::workerCallback)))
                FileUploadType.ASSIGNMENT -> {
                    when {
                        course == null -> {
                            _events.postValue(Event(ShareExtensionAction.ShowToast(resources.getString(R.string.noCourseSelected))))
                        }
                        assignment == null -> {
                            _events.postValue(Event(ShareExtensionAction.ShowToast(resources.getString(R.string.noAssignmentSelected))))
                        }
                        else -> {
                            _events.postValue(Event(ShareExtensionAction.ShowAssignmentUploadDialog(course, assignment, it, uploadType, this::uploadDialogCallback, this::workerCallback)))
                        }
                    }
                }
                else -> _events.postValue(Event(ShareExtensionAction.ShowToast(resources.getString(R.string.notSupported))))
            }
        } ?: _events.postValue(Event(ShareExtensionAction.ShowToast(resources.getString(R.string.errorOccurred))))
    }

    fun finish() {
        _events.postValue(Event(ShareExtensionAction.Finish))
    }

    fun showSuccessDialog(fileUploadType: FileUploadType) {
        _events.postValue(Event(ShareExtensionAction.ShowSuccessDialog(fileUploadType)))
    }

    fun showErrorDialog(fileUploadType: FileUploadType) {
        _events.postValue(Event(ShareExtensionAction.ShowErrorDialog(fileUploadType)))
    }

    fun showConfetti() {
        _events.postValue(Event(ShareExtensionAction.ShowConfetti))
    }

    private fun uploadDialogCallback(event: Int) {
        when (event) {
            FileUploadDialogFragment.EVENT_DIALOG_CANCELED -> finish()
        }
    }

    private fun showProgressDialog(uuid: UUID) {
        _events.postValue(Event(ShareExtensionAction.ShowProgressDialog(uuid)))
    }

    private fun workerCallback(uuid: UUID, liveData: LiveData<WorkInfo>) {
        showProgressDialog(uuid)
    }

}

sealed class ShareExtensionAction {
    data class ShowAssignmentUploadDialog(val course: CanvasContext, val assignment: Assignment, val fileUri: Uri, val uploadType: FileUploadType, val dialogCallback: (Int) -> Unit, val workerCallback: (UUID, LiveData<WorkInfo>) -> Unit) : ShareExtensionAction()
    data class ShowMyFilesUploadDialog(val fileUri: Uri, val dialogCallback: (Int) -> Unit, val workerCallback: (UUID, LiveData<WorkInfo>) -> Unit) : ShareExtensionAction()
    data class ShowProgressDialog(val uuid: UUID) : ShareExtensionAction()
    data class ShowSuccessDialog(val fileUploadType: FileUploadType) : ShareExtensionAction()
    data class ShowErrorDialog(val fileUploadType: FileUploadType) : ShareExtensionAction()
    object Finish : ShareExtensionAction()
    object ShowConfetti : ShareExtensionAction()
    data class ShowToast(val toast: String) : ShareExtensionAction()
}