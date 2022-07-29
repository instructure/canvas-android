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

package com.instructure.pandautils.features.shareextension.success

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.FileUploadType
import com.instructure.pandautils.mvvm.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareExtensionSuccessDialogViewModel @Inject constructor(
        private val resources: Resources
) : ViewModel() {

    val data: LiveData<ShareExtensionSuccessViewData>
        get() = _data
    private val _data = MutableLiveData<ShareExtensionSuccessViewData>()

    val events: LiveData<Event<ShareExtensionSuccessAction>>
        get() = _events
    private val _events = MutableLiveData<Event<ShareExtensionSuccessAction>>()

    fun initData(uploadType: FileUploadType) {
        _data.postValue(ShareExtensionSuccessViewData(
                dialogTitle = if (uploadType == FileUploadType.ASSIGNMENT) resources.getString(R.string.submission) else resources.getString(R.string.fileUpload),
                subTitle = if (uploadType == FileUploadType.ASSIGNMENT) resources.getString(R.string.submissionSuccessTitle) else resources.getString(R.string.fileUploadSuccess),
                description = if (uploadType == FileUploadType.ASSIGNMENT) resources.getString(R.string.submissionSuccessMessage) else resources.getString(R.string.filesUploadedSuccessfully)
        ))

        if (uploadType == FileUploadType.ASSIGNMENT) {
            _events.postValue(Event(ShareExtensionSuccessAction.ShowConfetti))
        }
    }

    fun onDoneClick() {
        _events.postValue(Event(ShareExtensionSuccessAction.Done))
    }
}