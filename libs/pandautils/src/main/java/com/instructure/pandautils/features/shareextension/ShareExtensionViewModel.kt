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
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.features.file.upload.FileUploadType
import com.instructure.pandautils.mvvm.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareExtensionViewModel @Inject constructor(
        private val apiPrefs: ApiPrefs,
        private val resources: Resources
) : ViewModel() {

    var uri: Uri? = null

    val events: LiveData<Event<ShareExtensionAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ShareExtensionAction>>()

    fun checkIfLoggedIn(): Boolean {
        return !TextUtils.isEmpty(apiPrefs.getValidToken())
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
        uri?.let {
            _events.postValue(Event(ShareExtensionAction.ShowUploadDialog(course, assignment, it, uploadType, this::uploadDialogCallback)))
        } ?: _events.postValue(Event(ShareExtensionAction.ShowToast(resources.getString(R.string.errorOccurred))))
    }

    fun finish() {
        _events.postValue(Event(ShareExtensionAction.Finish))
    }

    private fun showProgressDialog() {
        _events.postValue(Event(ShareExtensionAction.ShowProgressDialog))
    }

    private fun uploadDialogCallback(event: Int) {
        when (event) {
            FileUploadDialogFragment.EVENT_DIALOG_CANCELED -> finish()
            FileUploadDialogFragment.EVENT_ON_UPLOAD_BEGIN -> showProgressDialog()
        }
    }
}

sealed class ShareExtensionAction {
    data class ShowUploadDialog(val course: CanvasContext?, val assignment: Assignment?, val fileUri: Uri, val uploadType: FileUploadType, val dialogCallback: (Int) -> Unit) : ShareExtensionAction()
    object ShowProgressDialog: ShareExtensionAction()
    object ShowErrorDialog: ShareExtensionAction()
    object ShowSuccessDialog: ShareExtensionAction()
    object Finish: ShareExtensionAction()
    data class ShowToast(val toast: String) : ShareExtensionAction()
}